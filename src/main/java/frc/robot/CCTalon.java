package frc.robot;

// import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.*;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.sensors.CANCoder;


public class CCTalon extends WPI_TalonSRX {

    // private CANCoder cancoder;

    public CCTalon(int port, boolean reverse) {
        super(port);
        setInverted(reverse);

        // cancoder = new CANCoder(port);
    }

    public void set(double speed) {
        set(ControlMode.PercentOutput, speed);
    }

    // public CANCoder getCanCoder() {
    //     return cancoder;
    // }
    
}
