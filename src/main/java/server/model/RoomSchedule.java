package server.model;

/**
 *
 * @author skdks
 */
public class RoomSchedule {
        private String scheduleId;
    private String roomNumber;
    private String type; // "Cleaning"(청소), "Maintenance"(수리), "Manual"(임의설정)
    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
    private String note;

    public RoomSchedule(String scheduleId, String roomNumber, String type, String startDate, String endDate, String note) {
        this.scheduleId = scheduleId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
    }

    public String getScheduleId() { return scheduleId; }
    public String getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", scheduleId, roomNumber, type, startDate, endDate, note);
    }
}
