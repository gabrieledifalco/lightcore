// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package repository;

public class QueryExecutionResult {
    private int affectedRows;
    private Long insertId;
    private String message;
    private String error;

    public QueryExecutionResult(int affectedRows, Long insertId, String message, String error) {
        this.affectedRows = affectedRows;
        this.insertId = insertId;
        this.message = message;
        this.error = error;
    }

    public int getAffectedRows() {
        return affectedRows;
    }

    public Long getInsertId() {
        return insertId;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }

    public void setInsertId(Long insertId) {
        this.insertId = insertId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "affectedRaws: " + affectedRows +
                ", insertId: " + (insertId != null ? insertId : "null") +
                ", message: '" + message + '\'' +
                ", error: '" + error + '\'';
    }
}
