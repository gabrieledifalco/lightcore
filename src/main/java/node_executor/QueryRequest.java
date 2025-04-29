// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Personal Use & Attribution
// SPDX-License-Identifier: LicenseRef-LightCore

package node_executor;

import java.util.List;

public class QueryRequest {
    private String sql;
    private List<Object> params;

    public QueryRequest(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
