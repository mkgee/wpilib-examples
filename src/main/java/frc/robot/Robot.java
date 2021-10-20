/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.parent.*;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.sensors.CANCoderFaults;

import edu.wpi.first.wpilibj.Compressor;
// import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.networktables.NetworkTableEntry;
import com.ctre.phoenix.motorcontrol.Faults;
import static frc.robot.Chassis.*;
import java.util.Map;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot implements RobotMap, ControMap {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String kResetPIDs = "Reset PIDs";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private Compressor c = new Compressor();
  private ShuffleboardTab summaryTab = Shuffleboard.getTab("Summary");
  private SimpleWidget faultWidget;
  private NetworkTableEntry faultEntry;
  private NetworkTableEntry faultValueEntry;

  int alliance;
  double spdmlt = 1;

  private final Field2d field = new Field2d();
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.addOption("My Auto", kCustomAuto);
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("Reset PID Values", kResetPIDs);
    SmartDashboard.putNumber("Distance", 0.0);
    SmartDashboard.putNumber("Angle", 0.0);
    SmartDashboard.putData("Auto choices", m_chooser);
    Chassis.reset();

    switch(DriverStation.getInstance().getAlliance()){
      case Blue:
        alliance = 1;
      break;

      case Red:
        alliance = 0; 
      break;
      
      case Invalid:
        alliance = -1;
      break;
    }
    
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

  
  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    
    
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
    if(RobotMap.COMPRESSOR_ENABLE)
      c.start();
    else 
      c.stop();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    Chassis.reset();
    m_autoSelected = m_chooser.getSelected();
    System.out.println("Auto selected: " + m_autoSelected);
    
    double dist = SmartDashboard.getNumber("Distance", 0);
    double angl = SmartDashboard.getNumber("Angle", 0);
    switch (m_autoSelected) {
      case kCustomAuto:
        break;
      case kDefaultAuto:
        Chassis.driveDist(dist, 0.05, 0.04, 0.25, false);
        Chassis.turnToAngle(angl, 0.005, 0.5, 0.25, false);
        break;
      case kResetPIDs:
        break;
      default:
        break;
    }

  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    // System.out.println("method teleopPeriodic() entry");
    Chassis.axisDrive(OI.axis(ControMap.L_JOYSTICK_VERTICAL),
                      OI.axis(ControMap.R_JOYSTICK_HORIZONTAL), 0.5);
    
    if(OI.axis(ControMap.RT) > 0.5){
      Chassis.setFastMode(true);
      Chassis.setFactor(0.048);
    }else{  
      Chassis.setFastMode(false);
      Chassis.setFactor(0.109);
    }

  }

  /**
   * This function is called right after disabling
   */
  @Override
  public void disabledInit() {
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

}
