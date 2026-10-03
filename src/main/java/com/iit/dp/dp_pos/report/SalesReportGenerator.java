package com.iit.dp.dp_pos.report;

import java.time.LocalDate;
import java.util.List;

public interface SalesReportGenerator {
    List<SalesReportRow> generateReport(LocalDate start, LocalDate end);

    /** table-sales-report-generator false return kore
     * visual ae report show kore*/
    boolean export(List<SalesReportRow> rows, LocalDate start, LocalDate end);
    String getFormatName();
}
