package com.app.stonemart;

public class LeaveTypeItem {
    private final int id;
    private final String code;
    private final String name;
    private final String allowanceDesc;
    private final String salaryTreatment; // PAID, UNPAID
    private final boolean balanceTracked;
    private final boolean halfDayAllowed;
    private final boolean statutory;
    private final String description;

    public LeaveTypeItem(int id, String code, String name, String allowanceDesc,
                         String salaryTreatment, boolean balanceTracked,
                         boolean halfDayAllowed, boolean statutory, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.allowanceDesc = allowanceDesc;
        this.salaryTreatment = salaryTreatment;
        this.balanceTracked = balanceTracked;
        this.halfDayAllowed = halfDayAllowed;
        this.statutory = statutory;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAllowanceDesc() {
        return allowanceDesc;
    }

    public String getSalaryTreatment() {
        return salaryTreatment;
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(salaryTreatment);
    }

    public boolean isBalanceTracked() {
        return balanceTracked;
    }

    public boolean isHalfDayAllowed() {
        return halfDayAllowed;
    }

    public boolean isStatutory() {
        return statutory;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
