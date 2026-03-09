package com.m3verificaciones.appweb.consumption.dto;

import java.util.List;

public class ExcelExportRequestDTO {
    private List<String> selectedFields;

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        this.selectedFields = selectedFields;
    }
}
