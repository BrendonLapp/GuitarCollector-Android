package com.example.b_lap.guitarcollector;

public class GuitarPost {

    //these need to be named the same as to what is in the database for its contents names
        private String brand;
        private String model;
        private String type;
        private String serialNumber;
        private String tuning;
        private String stringGauge;
        private String userId;
        private String image; //I can't get the image upload to work please ignore this

        public GuitarPost() {}

        public GuitarPost(String brand, String model, String type, String serialNumber, String tuning, String stringGauge, String userId, String image) {
            this.brand = brand;
            this.model = model;
            this.type = type;
            this.serialNumber = serialNumber;
            this.tuning = tuning;
            this.stringGauge = stringGauge;
            this.userId = userId;
            this.image = image;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getTuning() {
        return tuning;
    }

    public void setTuning(String tuning) {
        this.tuning = tuning;
    }

    public String getStringGauge() {
        return stringGauge;
    }

    public void setStringGauge(String stringGauge) {
        this.stringGauge = stringGauge;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
