package comp3004.project.QotRT.cards;

public class StoryCard {
    int stages;
    String name;
    String foevalue;
    String type;
    public int getStages() {
        return stages;
    }

    public String getName() {
        return name;
    }

    public void setStages(int stages) {
        this.stages = stages;
    }

    public String getFoevalue() {
        return foevalue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFoevalue(String foevalue) {
        this.foevalue = foevalue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuffer display = new StringBuffer();
        display.append("---- " + name + " ----\n");
        display.append("--having battle points-- "+ stages + " ----\n");
        display.append("--foe  value at the bottom-- " + foevalue + " ----\n");

        /*
        for (Card card : realcards) {
            display.append(card + "\n");
        }

         */
        return display.toString();
    }
}
