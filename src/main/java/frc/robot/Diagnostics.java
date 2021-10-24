package frc.robot;

import static frc.robot.Chassis.bLeft;
import static frc.robot.Chassis.bRight;
import static frc.robot.Chassis.fLeft;
import static frc.robot.Chassis.fRight;
import static frc.robot.Chassis.motor2019;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.revrobotics.CANSparkMax.FaultID;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
public class Diagnostics {

    private final Field2d field = new Field2d();
    private ShuffleboardTab summaryTab = Shuffleboard.getTab("Summary");
    private NetworkTableEntry faultEntry;
    private CCSparkMax[] motors;
    private Map<String, NetworkTableEntry> motorFaultMap = new HashMap<>();
  
    public Diagnostics(CCSparkMax... motors) {
        this.motors = motors;
    }

    public void init() {
        motor2019.set(ControlMode.PercentOutput, 0);
        SmartDashboard.putData("Field", field);
        summaryTab = Shuffleboard.getTab("Summary");
        
        faultEntry = summaryTab
          .add("Fault Indicator", false)
          .withWidget(BuiltInWidgets.kBooleanBox)
          .getEntry();
    
        // faultValueEntry = summaryTab.add("Fault Value", 0)
        //   .withWidget(BuiltInWidgets.kDial)
        //   .withProperties(Map.of("Min", 0, "Max", 100))
        //   .getEntry();

        int row = 1;
        int col = 0;
        final int width = 3;
        for(CCSparkMax m : motors) {
            motorFaultMap.put(m.getName(), summaryTab.add(m.getName() + " faults", "")
            .withWidget(BuiltInWidgets.kTextView)
            .withPosition(col, row) 
            .withSize(width, 1)
            .getEntry() );

            if (col == 0) {
                col += width;
            } else {
                col = 0;
                row++;
            }

        }
        Shuffleboard.selectTab("summary");
    }
    
    private void updateStatus(CCSparkMax motor) {
        int fault = motor.getFaults();
        String faultMsg = "No fault";
        if (fault != 0) {
            faultMsg = Arrays.stream(FaultID.values())
                .filter(motor::getFault)
                .map(FaultID::name)
                .collect(joining(","));
        }
        // SmartDashboard.putString(motor.getName(), faultMsg);
        motorFaultMap.get(motor.getName()).setString(faultMsg);
    }

    public void updateStatus() {
        int bLeftFault = bLeft.getFaults();
        int bRightFault = bRight.getFaults();
        int fLeftFault = fLeft.getFaults();
        int fRightFault = fRight.getFaults();
        int allFaults = bLeftFault + bRightFault + fLeftFault + fRightFault;

        // boolean status
        faultEntry.setBoolean(allFaults == 0);

        // update status on SparkMax controllers
        Stream.of(bLeft, bRight, fLeft, fRight).forEach(this::updateStatus);
        
        // update status on phoenix talon controllers
        Faults faults= new Faults();
        motor2019.getFaults(faults);
        SmartDashboard.putNumber("Talon faults", faults.toBitfield());
    }
}
