package com.example.springboot.service;

import com.example.springboot.dto.AdminReservationResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminExportService {

    private final AdminReservationService reservationService;

    public AdminExportService(AdminReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public String exportReservationsToCsv(String authorization) {
        List<AdminReservationResponse> reservations = reservationService.reservations(authorization, null, null, null, null);

        StringBuilder csv = new StringBuilder();
        // CSV Header
        csv.append("Type,ID,Name,Phone,Email,ProjectKey,Date,Time,Amount,Payment,Provider,Account,CreatedAt\n");

        // CSV Rows
        for (AdminReservationResponse res : reservations) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsv(res.type()),
                res.id(),
                escapeCsv(res.name()),
                escapeCsv(res.phoneNumber()),
                escapeCsv(res.userEmail()),
                escapeCsv(res.projectKey()),
                res.date(),
                res.time(),
                res.amount(),
                escapeCsv(res.paymentMethodType()),
                escapeCsv(res.paymentProviderName()),
                escapeCsv(res.bankAccountMasked()),
                res.createdAt()
            ));
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "-";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
