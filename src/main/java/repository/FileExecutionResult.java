// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package repository;

import java.util.List;
import java.util.Map;

public class FileExecutionResult {
    private boolean success;
    private List<FileOperationResult> results;

    public static class FileOperationResult {
        private String action;
        private String path;
        private Map<String, Object> result;
        private String error;

        public String getAction() {
            return action;
        }

        public String getPath() {
            return path;
        }

        public Map<String, Object> getResult() {
            return result;
        }

        public String getError() {
            return error;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setResult(Map<String, Object> result) {
            this.result = result;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public List<FileOperationResult> getResults() {
        return results;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setResults(List<FileOperationResult> results) {
        this.results = results;
    }
}
