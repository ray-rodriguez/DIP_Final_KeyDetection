/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camera;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author MQ0162246
 */
public class RubberBandSelection {
    
    DragContext dragContext = new DragContext();
    private Rectangle rect = new Rectangle();
    Group group;
    public RubberBandSelection(Group group)
    {
        this.group = group;
        rect = new Rectangle(0, 0, 0, 0);
        rect.setStroke(Color.RED);
        rect.setStrokeWidth(1);
        rect.getStrokeDashArray().addAll(3.0,7.0,3.0,7.0);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTGRAY.deriveColor(0, 1.2, 1, 0.3));
        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }

    public Bounds getBounds()  {
        return rect.getBoundsInParent();
    }
    
    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.isSecondaryButtonDown()) {
                return;
            }
            // remove old rect
            getRect().setX(0);
            getRect().setY(0);
            getRect().setWidth(0);
            getRect().setHeight(0);
            group.getChildren().remove(getRect());
            // prepare new drag operation
            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();
            getRect().setX(dragContext.mouseAnchorX);
            getRect().setY(dragContext.mouseAnchorY);
            getRect().setWidth(0);
            getRect().setHeight(0);
            group.getChildren().add(getRect());
        }
    };
    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.isSecondaryButtonDown()) {
                return;
            }
            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;
            if (offsetX > 0) {
                getRect().setWidth(offsetX);
            } else {
                getRect().setX(event.getX());
                getRect().setWidth(dragContext.mouseAnchorX - getRect().getX());
            }
            if (offsetY > 0) {
                getRect().setHeight(offsetY);
            } else {
                getRect().setY(event.getY());
                getRect().setHeight(dragContext.mouseAnchorY - getRect().getY());
            }
        }
    };
    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.isSecondaryButtonDown()) {
                return;
            }
        }
    };

    /**
     * @return the rect
     */
    public Rectangle getRect() {
        return rect;
    }

    /**
     * @param rect to set
     */
    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    private static final class DragContext {

        public double mouseAnchorX;
        public double mouseAnchorY;
    }
}
