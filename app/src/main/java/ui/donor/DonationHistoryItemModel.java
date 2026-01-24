package ui.donor;

public class DonationHistoryItemModel {

    private String foodName;
    private String dateTime;
    private String location;
    private String deliveryStatus;
    private String imageUrl;

    public DonationHistoryItemModel(String foodName,
                                    String dateTime,
                                    String location,
                                    String deliveryStatus,
                                    String imageUrl) {
        this.foodName = foodName;
        this.dateTime = dateTime;
        this.location = location;
        this.deliveryStatus = deliveryStatus;
        this.imageUrl = imageUrl;
    }

    public String getFoodName() { return foodName; }
    public String getDateTime() { return dateTime; }
    public String getLocation() { return location; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getImageUrl() { return imageUrl; }
}
