package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CicilanStatus {
    private final int nomorCicilan;
    private final BigDecimal jumlah;
    private final LocalDate jatuhTempo;
    private final String status;

    public CicilanStatus(int nomorCicilan, BigDecimal jumlah, LocalDate jatuhTempo, boolean isLunas) {
        this.nomorCicilan = nomorCicilan;
        this.jumlah = jumlah;
        this.jatuhTempo = jatuhTempo;
        this.status = isLunas ? "Lunas" : "Belum Lunas";
    }

    // Getters ini WAJIB ada agar TableView bisa mengambil data
    public int getNomorCicilan() {
        return nomorCicilan;
    }

    public BigDecimal getJumlah() {
        return jumlah;
    }

    public LocalDate getJatuhTempo() {
        return jatuhTempo;
    }

    public String getStatus() {
        return status;
    }
}