package cn.sola97.vrchat.entity;

public class VRCResponse {
    public Success success;
    public String error;
    public String status_code;

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    @Override
    public String toString() {
        return "VRCResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", status_code='" + status_code + '\'' +
                '}';
    }

    public class Success {
        public String message;
        public Integer status_code;

        public Success() {
        }

        @Override
        public String toString() {
            return "Success{" +
                    "message='" + message + '\'' +
                    ", status_code=" + status_code +
                    '}';
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getStatus_code() {
            return status_code;
        }

        public void setStatus_code(Integer status_code) {
            this.status_code = status_code;
        }
    }
}
