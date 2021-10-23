package frc.robot;

import static frc.robot.Chassis.*;
import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.networktables.NetworkTableEntry;
import com.ctre.phoenix.motorcontrol.Faults;
import java.util.Map;

public class Diagnostics {

    private final Field2d field = new Field2d();
    private ShuffleboardTab summaryTab = Shuffleboard.getTab("Summary");
    private SimpleWidget faultWidget;
    private NetworkTableEntry faultEntry;
    private NetworkTableEntry faultValueEntry;
  
    public void init() {
        motor2019.set(ControlMode.PercentOutput, 0);
        SmartDashboard.putData("Field", field);
        summaryTab = Shuffleboard.getTab("Summary");
        
        faultEntry = summaryTab
          .add("Fault Indicator", false)
          .withWidget(BuiltInWidgets.kBooleanBox)
          .getEntry();
    
        faultValueEntry = summaryTab.add("Fault Value", 0)
          .withWidget(BuiltInWidgets.kDial)
          .withProperties(Map.of("Min", 0, "Max", 100))
          .getEntry();
    }
    
    public void updateStatus() {
        int bLeftFault = bLeft.getFaults();
        int bRightFault = bRight.getFaults();
        int fLeftFault = fLeft.getFaults();
        int fRightFault = fRight.getFaults();
        int allFaults = bLeftFault + bRightFault + fLeftFault + fRightFault;
        faultEntry.setBoolean(allFaults == 0);
        faultValueEntry.setDouble(allFaults);
    
        // SmartDashboard.putNumber("bLeft faults", Chassis.bLeft.getFaults());
        // SmartDashboard.putNumber("bLeft motor temperature", Chassis.bLeft.getMotorTemperature());
        // SmartDashboard.putNumber("bRight faults", Chassis.bRight.getFaults());
        // SmartDashboard.putNumber("fLeft faults", Chassis.fLeft.getFaults());
        // SmartDashboard.putNumber("fRight faults", Chassis.fRight.getFaults());
    
        // CANCoderFaults faults = new CANCoderFaults();
        // Chassis.motor2019.getCanCoder().getFaults(faults);
        // SmartDashboard.putNumber("Talon faults", faults.toBitfield());
        
        Faults faults= new Faults();
        motor2019.getFaults(faults);
        SmartDashboard.putNumber("Talon faults", faults.toBitfield());
    }
}
