package view;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Pane mit Eigenschaft zum Zoomen
 */
public class ZoomableScrollPane extends ScrollPane {
    // Aktueller Zoom-Faktor
    private double scaleValue = 1;
    // Stärke der Vergrößerung beim Drehen des Mausrads
    private double zoomIntensity = 0.04;
    private AnchorPane anchorPane;
    private Node zoomNode;

    /**
     * Konstruktor zum Anlegen von Pane mit Eigenschaft zum Zoomen
     * @param anchorPane
     */
    public ZoomableScrollPane(AnchorPane anchorPane) {
        super();
        this.anchorPane = anchorPane;
        this.zoomNode = new Group(anchorPane);
        setContent(outerNode(zoomNode));

        // Aktiviere Verschieben mit Maus
        setPannable(true);
        // Ausblenden der Scrollbars
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(true);
        setFitToWidth(true);
        updateScale();
    }

    /**
     * Leifert eine UI-Komponente, welche zentriert und scrollbar ist
     * @param node Knoten, auf dem die Zoom-Eigenschaft aktiviert werden soll
     * @return eine VBox, auf der man zoomen kann
     */
    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        // Scroll-Eigenschaft aktivieren
        outerNode.setOnScroll(e -> {
            // Verbrauche das Event
            e.consume();
            // Wird ausgefüht, wenn Mausrad gedreht wird
            // getTextDeltaY: wie weit das Mausrad gedreht wird
            // neuer Punnkt an Position der Maus
            onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
        });
        return outerNode;
    }

    /**
     *
     * @param outerNode ein Node, der in die Mitte einer VBox positioniert wird
     * @return eine VBox mit outerNode in der Mitte
     */
    private Node centeredNode(Node outerNode) {
        VBox vBox = new VBox(outerNode);
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-background-color: black");
        return vBox;
    }

    /**
     * Oberfläche mit aktuellem Zoom-Faktor anzeigen
     */
    private void updateScale() {
        anchorPane.setScaleX(scaleValue);
        anchorPane.setScaleY(scaleValue);
    }

    /**
     * Scroll-Aktion ausführen
     * @param wheelDelta - Drehung des Mausrades
     * @param mousePoint - Mausposition
     */
    private void onScroll(double wheelDelta, Point2D mousePoint) {
        // bestimme Zoom-Faktor in Abhängigkeit von "gescrollter Menge"
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        // sichtbare Bereiche innerhalb der Grenzen
        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // getHvalue, getVvalue: bezieht sich auf die aktuelle Scrollposition
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());
        // Skalierungsfaktor wird bestimmt
        scaleValue = scaleValue * zoomFactor;
        updateScale();
        // Oberfläche aktualisieren
        this.layout();

        // Umrechnen der Koordinaten zwischen anchorPane und zoomNode
        Point2D posInZoomTarget = anchorPane.parentToLocal(zoomNode.parentToLocal(mousePoint));

        Point2D adjustment = anchorPane.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // Scroll-Positionen neu berechnen
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }
}


// Quelle der Inspiration: https://stackoverflow.com/questions/39827911/javafx-8-scaling-zooming-scrollpane-relative-to-mouse-position