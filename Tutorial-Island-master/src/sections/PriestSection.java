package sections;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import util.Sleep;

import java.util.Arrays;
import java.util.List;

public final class PriestSection extends TutorialSection {

    private static final Area CHURCH_AREA = new Area(3120, 3103, 3128, 3110);

    private static final List<Position> PATH_TO_CHURCH = Arrays.asList(
            new Position(3131, 3124, 0),
            new Position(3134, 3121, 0),
            new Position(3134, 3117, 0),
            new Position(3132, 3114, 0),
            new Position(3130, 3111, 0),
            new Position(3130, 3108, 0),
            new Position(3129, 3106, 0)
    );

    public PriestSection() {
        super("Brother Brace");
    }

    @Override
    public final void onLoop() throws InterruptedException {
        if (pendingContinue()) {
            selectContinue();
            return;
        }

        switch (getProgress()) {
            case 550:
                if (getInstructor() == null) {
                    getWalking().walkPath(PATH_TO_CHURCH);
                } else if (!getMap().canReach(getInstructor())) {
                    getDoorHandler().handleNextObstacle(CHURCH_AREA);
                } else {
                    talkToInstructor();
                }
                break;
            case 560:
                getTabs().open(Tab.PRAYER);
                break;
            case 570:
                talkToInstructor();
                break;
            case 580:
                getTabs().open(Tab.FRIENDS);
                break;
            case 590:
                // Direct widget interaction for opening Ignore tab
                // Try different widget IDs that might contain the ignore tab
                RS2Widget ignoreWidget = getWidgets().get(429, 7);
                if (ignoreWidget != null && ignoreWidget.isVisible()) {
                    ignoreWidget.interact();
                    Sleep.sleepUntil(() -> getProgress() != 590, 3000);
                } else {
                    // Alternative widget ID for ignore tab
                    RS2Widget alternativeIgnore = getWidgets().get(548, 67);
                    if (alternativeIgnore != null && alternativeIgnore.isVisible()) {
                        alternativeIgnore.interact();
                        Sleep.sleepUntil(() -> getProgress() != 590, 3000);
                    } else {
                        // Try yet another alternative widget
                        RS2Widget anotherAlternative = getWidgets().get(161, 35);
                        if (anotherAlternative != null && anotherAlternative.isVisible()) {
                            anotherAlternative.interact();
                            Sleep.sleepUntil(() -> getProgress() != 590, 3000);
                        }
                    }
                }
                break;
            case 600:
                talkToInstructor();
                break;
            case 610:
                if (getDoorHandler().handleNextObstacle(new Position(3122, 3101, 0))) {
                    Sleep.sleepUntil(() -> getProgress() != 610, 5000, 600);
                }
                break;
        }
    }
}
