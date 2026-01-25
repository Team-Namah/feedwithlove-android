package ui.receiver;

public class FoodItem {

    private final String foodId;
    private final String foodName;
    private final String quantity;
    private final String location;
    private final String time;
    private final String donorInfo;
    private final String imageUrl;

    public FoodItem(String foodId, String foodName, String quantity,
                    String location, String time,
                    String donorInfo, String imageUrl) {

        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.location = location;
        this.time = time;
        this.donorInfo = donorInfo;
        this.imageUrl = imageUrl;
    }

    public String getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public String getQuantity() { return quantity; }
    public String getLocation() { return location; }
    public String getTime() { return time; }
    public String getDonorInfo() { return donorInfo; }
    public String getImageUrl() { return imageUrl; }
}
