package frc.robot;

import static frc.robot.Chassis.bLeft;
import static frc.robot.Chassis.bRight;
import static frc.robot.Chassis.fLeft;
import static frc.robot.Chassis.fRight;
import static frc.robot.Chassis.motor2019;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.FaultID;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
public class Diagnostics {

    enum DataType {FAULTS, STICKY_FAULTS, TEMP, INVERTED_STATE, POSITION, VELOCITY};

    private final Field2d field = new Field2d();
    private final ShuffleboardTab summaryTab = Shuffleboard.getTab("Summary");
    private final ShuffleboardTab motorTab = Shuffleboard.getTab("Motors");
    private NetworkTableEntry faultEntry;
    private CCSparkMax[] motors;
    
    private Map<String, Map<DataType, NetworkTableEntry>> motorEntryMap = new HashMap<>();
  
    public Diagnostics(CCSparkMax... motors) {
        this.motors = motors;
    }

    public void init() {
        motor2019.set(ControlMode.PercentOutput, 0);
        SmartDashboard.putData("Field", field);
        // summaryTab = Shuffleboard.getTab("Summary");
        
        faultEntry = summaryTab
          .add("Fault Indicator", false)
          .withWidget(BuiltInWidgets.kBooleanBox)
          .getEntry();

        int row = 0;
        final int faultsWidth = 2;
        // for each motor: Faults, Sticky Faults, Temp, Inverted state, position, velocity
        for(CCSparkMax m : motors) {
            int col = 0;
            Map<DataType, NetworkTableEntry> entryMap = new EnumMap<>(DataType.class);

            // initialize motorEntryMap
            motorEntryMap.put(m.getName(), entryMap);

            final String name = m.getName();
            final String shortName = m.getShortName();

            // FAULTS
            entryMap.put(DataType.FAULTS, motorTab.add(name + " faults", "")
            .withWidget(BuiltInWidgets.kTextView)
            .withPosition(col, row) 
            .withSize(faultsWidth, 1)
            .getEntry() );
            col += faultsWidth;
            
            // STICKY_FAULTS
            entryMap.put(DataType.STICKY_FAULTS, motorTab.add(shortName + " sticky faults", "")
            .withWidget(BuiltInWidgets.kTextView)
            .withPosition(col, row) 
            .withSize(faultsWidth, 1)
            .getEntry() );
            col += faultsWidth;
            
            // INVERTED_STATE
            entryMap.put(DataType.INVERTED_STATE, motorTab.add(shortName + " inv. state", "")
            .withWidget(BuiltInWidgets.kTextView)
            .withPosition(col++, row) 
            .withSize(1, 1)
            .getEntry() );

            // POSITION
            entryMap.put(DataType.POSITION, motorTab.add(shortName + "  position", 0)
            .withWidget(BuiltInWidgets.kTextView)
            .withPosition(col++, row) 
            .withSize(1, 1)
            .getEntry() );

            // TEMP
            entryMap.put(DataType.TEMP, motorTab.add(shortName + " temp", 0)
            .withWidget(BuiltInWidgets.kDial)
            .withPosition(col++, row) 
            .withSize(1, 1)
            .withProperties(Map.of("Min", 10, "Max", 100))  //celsius
            .getEntry() );
            
            
            // VELOCITY
            entryMap.put(DataType.VELOCITY, motorTab.add(shortName + "  velocity", 0)
            .withWidget(BuiltInWidgets.kDial)
            .withPosition(col++, row) 
            .withSize(1, 1)
            .getEntry() );
            
            row++;
            
        }

        Shuffleboard.selectTab("Motors");
    }

    private void updateFaultStatus(NetworkTableEntry entry, Supplier<Short> faultsSupplier, 
                    Function<CANSparkMax.FaultID, Boolean> faultSupplier) {
                        int fault = faultsSupplier.get();
                        String faultMsg = "No fault";
                        if (fault != 0) {
                            faultMsg = Arrays.stream(FaultID.values())
                                .filter(v -> faultSupplier.apply(v))
                                .map(FaultID::name)
                                .collect(joining(","));
                        }
                        entry.setString(faultMsg); 
    }
    
    private NetworkTableEntry getEntry(CCSparkMax motor, DataType type) {
        return motorEntryMap.get(motor.getName()).get(type);
        
    }

    private void updateFaultStatus(CCSparkMax motor, DataType type) {
        if (type.equals(DataType.FAULTS)) {
            updateFaultStatus(getEntry(motor,type), motor::getFaults, motor::getFault);
        } else {
            updateFaultStatus(getEntry(motor,type), motor::getStickyFaults, motor::getStickyFault);
        }
    }

    private void updateDoubleStatus(CCSparkMax motor, DataType type) {
        
        switch(type) {
            case TEMP:
                getEntry(motor,type).setDouble(motor.getMotorTemperature());
                break;
            case POSITION:
                getEntry(motor,type).setString(Double.toString(motor.getEncoder().getPosition()));
                break;
            case VELOCITY:
                getEntry(motor,type).setDouble(motor.getEncoder().getVelocity());
                break;
            default:
                break;
        }
    }

    private void updateStatus(CCSparkMax motor, DataType type) {

        switch(type) {
            case FAULTS:
            case STICKY_FAULTS:
                updateFaultStatus(motor, type);
                break;
            case TEMP:
            case POSITION:
            case VELOCITY:
                updateDoubleStatus(motor,type);
                break;
            case INVERTED_STATE: {
                String msg = motor.getInverted() ? "inverted" : "";
                getEntry(motor, type).setString(msg);
            }
            break;
        }
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
        Stream.of(bLeft, bRight, fLeft, fRight).forEach(motor -> {
            Arrays.stream(DataType.values()).forEach(type -> updateStatus(motor,type));
        });
        
        // update status on phoenix talon controllers
        Faults faults= new Faults();
        motor2019.getFaults(faults);
        SmartDashboard.putNumber("Talon faults", faults.toBitfield());
    }
}
