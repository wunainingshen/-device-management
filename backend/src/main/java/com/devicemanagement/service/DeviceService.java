package com.devicemanagement.service;

import com.devicemanagement.entity.Device;
import com.devicemanagement.repository.DeviceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
    }

    public Device createDevice(Device device) {
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device updatedDevice) {
        Device device = getDeviceById(id);
        if (updatedDevice.getName() != null) device.setName(updatedDevice.getName());
        if (updatedDevice.getModel() != null) device.setModel(updatedDevice.getModel());
        if (updatedDevice.getCategory() != null) device.setCategory(updatedDevice.getCategory());
        if (updatedDevice.getBrand() != null) device.setBrand(updatedDevice.getBrand());
        if (updatedDevice.getStatus() != null) device.setStatus(updatedDevice.getStatus());
        if (updatedDevice.getDescription() != null) device.setDescription(updatedDevice.getDescription());
        if (updatedDevice.getLocation() != null) device.setLocation(updatedDevice.getLocation());
        if (updatedDevice.getPurchaseDate() != null) device.setPurchaseDate(updatedDevice.getPurchaseDate());
        if (updatedDevice.getWarrantyExpiry() != null) device.setWarrantyExpiry(updatedDevice.getWarrantyExpiry());
        device.setUpdatedAt(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public List<Device> searchDevices(String name, String category, String status) {
        return deviceRepository.searchDevices(name, category, status);
    }

    // Export to Excel
    public byte[] exportToExcel() throws IOException {
        List<Device> devices = deviceRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("设备数据");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {"ID", "名称", "型号", "分类", "品牌", "状态", "位置", "描述", "购买日期", "保修到期"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (Device device : devices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(device.getId());
            row.createCell(1).setCellValue(device.getName());
            row.createCell(2).setCellValue(device.getModel() != null ? device.getModel() : "");
            row.createCell(3).setCellValue(device.getCategory() != null ? device.getCategory() : "");
            row.createCell(4).setCellValue(device.getBrand() != null ? device.getBrand() : "");
            row.createCell(5).setCellValue(device.getStatus() != null ? device.getStatus() : "");
            row.createCell(6).setCellValue(device.getLocation() != null ? device.getLocation() : "");
            row.createCell(7).setCellValue(device.getDescription() != null ? device.getDescription() : "");
            row.createCell(8).setCellValue(device.getPurchaseDate() != null ? device.getPurchaseDate().format(dtf) : "");
            row.createCell(9).setCellValue(device.getWarrantyExpiry() != null ? device.getWarrantyExpiry().format(dtf) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    // Import from Excel
    @Transactional
    public List<Device> importFromExcel(MultipartFile file) throws IOException {
        List<Device> devices = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Device device = new Device();
            device.setName(getCellValueAsString(row.getCell(1)));
            device.setModel(getCellValueAsString(row.getCell(2)));
            device.setCategory(getCellValueAsString(row.getCell(3)));
            device.setBrand(getCellValueAsString(row.getCell(4)));
            device.setStatus(getCellValueAsString(row.getCell(5)));
            device.setLocation(getCellValueAsString(row.getCell(6)));
            device.setDescription(getCellValueAsString(row.getCell(7)));

            String purchaseDateStr = getCellValueAsString(row.getCell(8));
            if (!purchaseDateStr.isEmpty()) {
                try {
                    device.setPurchaseDate(LocalDateTime.parse(purchaseDateStr, dtf));
                } catch (Exception e) {
                    device.setPurchaseDate(null);
                }
            }

            String warrantyStr = getCellValueAsString(row.getCell(9));
            if (!warrantyStr.isEmpty()) {
                try {
                    device.setWarrantyExpiry(LocalDateTime.parse(warrantyStr, dtf));
                } catch (Exception e) {
                    device.setWarrantyExpiry(null);
                }
            }

            device.setCreatedAt(LocalDateTime.now());
            device.setUpdatedAt(LocalDateTime.now());
            devices.add(deviceRepository.save(device));
        }

        workbook.close();
        return devices;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
