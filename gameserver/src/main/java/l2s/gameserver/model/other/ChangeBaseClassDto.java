package l2s.gameserver.model.other;

public class ChangeBaseClassDto {
    private final int classId;
    private final String className;

    public ChangeBaseClassDto(int classId, String className) {
        this.classId = classId;
        this.className = className;
    }

    public int getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }
}
