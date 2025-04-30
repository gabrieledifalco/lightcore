// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class SelectQueryResult extends QueryExecutionResult{
    private List<Map<String, Object>> rows;

    @JsonCreator
    public SelectQueryResult(@JsonProperty("rowCount") int rowCount, @JsonProperty("rows") List<Map<String, Object>> rows) {
        super(rowCount, null, rowCount > 0 ? rowCount + " rows found" : "No rows found", null);
        this.rows = rows;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public boolean hasRows() {
        return rows != null && !rows.isEmpty();
    }

    public Map<String, Object> getFirstRow() {
        return hasRows() ? rows.get(0) : null;
    }

    public Object getValue(int rowIndex, String columnName) {
        if(rows != null && rowIndex < rows.size()) {
            return rows.get(rowIndex).get(columnName);
        }

        return null;
    }
}
