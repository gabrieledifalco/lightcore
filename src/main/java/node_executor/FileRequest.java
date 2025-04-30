// LightCore Framework
// (c) 2025 Gabriele Difalco
// Licensed under the LightCore License – Limited Source Use
// SPDX-License-Identifier: LicenseRef-LightCore

package node_executor;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileRequest {
    private String action; // type of operation (read, write, delete, etc...)
    private String path; // path of the file
    private String encoding; // type of encoding ( utf8, etc...), optional
    private String content; // content of the file
    private String newPath; // new path of the file
    private Map<String, Object> options;

    public FileRequest(String action, String path) {
        this.action = action;
        this.path = path;
    }

    public FileRequest(String action, String path, String encoding, String content, String newPath, Map<String, Object> options) {
        this.action = action;
        this.path = path;
        this.encoding = encoding;
        this.content = content;
        this.newPath = newPath;
        this.options = options;
    }

    public String getAction() {
        return action;
    }

    public String getPath() {
        return path;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getContent() {
        return content;
    }

    public String getNewPath() {
        return newPath;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
