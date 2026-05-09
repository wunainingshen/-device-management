package com.devicemanagement.config;

import com.devicemanagement.entity.Device;
import com.devicemanagement.entity.User;
import com.devicemanagement.repository.DeviceRepository;
import com.devicemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setIsOnline(false);
            userRepository.save(admin);
            System.out.println("Default admin account created: admin / admin123");
        }

        // Create demo user if not exists
        if (!userRepository.existsByUsername("user1")) {
            User user = new User();
            user.setUsername("user1");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user1@example.com");
            user.setNickname("测试用户");
            user.setRole("USER");
            user.setEnabled(true);
            user.setIsOnline(false);
            userRepository.save(user);
            System.out.println("Default user account created: user1 / user123");
        }

        // Create demo devices if none exist
        if (deviceRepository.count() == 0) {
            String[][] deviceData = {
                {"ThinkPad X1 Carbon", "X1C Gen 11", "办公设备", "Lenovo", "NORMAL", "笔记本电脑", "A栋301"},
                {"Dell U2723QE", "U2723QE", "办公设备", "Dell", "NORMAL", "4K显示器", "A栋302"},
                {"HP LaserJet Pro", "M404dn", "办公设备", "HP", "FAULT", "激光打印机", "B栋101"},
                {"Cisco Catalyst 9200", "C9200-24T", "网络设备", "Cisco", "NORMAL", "核心交换机", "机房A"},
                {"APC Smart-UPS 1500", "SMT1500IC", "网络设备", "APC", "MAINTENANCE", "UPS电源", "机房A"},
                {"MacBook Pro 16", "M3 Pro", "办公设备", "Apple", "NORMAL", "开发用笔记本", "C栋201"},
                {"Synology DS923+", "DS923+", "网络设备", "Synology", "NORMAL", "NAS存储服务器", "机房B"},
                {"Canon imageRUNNER", "C5535i", "办公设备", "Canon", "FAULT", "多功能一体机", "A栋大厅"},
                {"Yealink SIP-T58A", "T58A", "办公设备", "Yealink", "NORMAL", "IP电话", "B栋201"},
                {"Hikvision DS-2CD2T47", "DS-2CD2T47G2", "其他", "Hikvision", "NORMAL", "网络摄像头", "园区各入口"}
            };

            for (String[] data : deviceData) {
                Device device = new Device();
                device.setName(data[0]);
                device.setModel(data[1]);
                device.setCategory(data[2]);
                device.setBrand(data[3]);
                device.setStatus(data[4]);
                device.setDescription(data[5]);
                device.setLocation(data[6]);
                device.setPurchaseDate(LocalDateTime.now().minusMonths((long) (Math.random() * 12)));
                device.setWarrantyExpiry(LocalDateTime.now().plusMonths((long) (Math.random() * 36)));
                deviceRepository.save(device);
            }
            System.out.println("Demo devices created: " + deviceData.length + " devices");
        }
    }
}
