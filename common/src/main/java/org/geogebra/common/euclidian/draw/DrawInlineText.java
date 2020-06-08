package org.geogebra.common.euclidian.draw;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.awt.GShape;
import org.geogebra.common.euclidian.BoundingBox;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianBoundingBoxHandler;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.RotatableBoundingBox;
import org.geogebra.common.euclidian.inline.InlineTextController;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInlineText;

/**
 * Class that handles drawing inline text elements.
 */
public class DrawInlineText extends Drawable implements DrawInline, HasFormat {

	public static final int PADDING = 8;

	private GeoInlineText text;
	private InlineTextController textController;

	private final TransformableRectangle rectangle;

	/**
	 * Create a new DrawInlineText instance.
	 *
	 * @param view view
	 * @param text geo element
	 */
	public DrawInlineText(EuclidianView view, GeoInlineText text) {
		super(view, text);
		rectangle = new TransformableRectangle(view, text);
		this.text = text;
		this.textController = view.getApplication().createInlineTextController(view, text);
		createEditor();
		update();
	}

	private void createEditor() {
		if (textController != null) {
			textController.create();
		}
	}

	@Override
	public void update() {
		rectangle.updateSelfAndBoundingBox();

		GPoint2D point = text.getLocation();
		if (textController != null && point != null) {
			double angle = text.getAngle();
			double width = text.getWidth();
			double height = text.getHeight();

			textController.setLocation(view.toScreenCoordX(point.getX()),
					view.toScreenCoordY(point.getY()));
			textController.setHeight((int) (height - 2 * PADDING));
			textController.setWidth((int) (width - 2 * PADDING));
			textController.setAngle(angle);
			if (text.updateFontSize()) {
				updateContent();
			}
		}
	}

	@Override
	public void updateContent() {
		if (textController != null) {
			textController.updateContent();
		}
	}

	@Override
	public void toBackground() {
		if (textController != null) {
			textController.toBackground();
		}
	}

	@Override
	public void toForeground(int x, int y) {
		if (textController != null) {
			GPoint2D p = rectangle.getInversePoint(x - PADDING, y - PADDING);
			textController.toForeground((int) p.getX(), (int) p.getY());
		}
	}

	/**
	 * @param x x mouse coordinate in pixels
	 * @param y y mouse coordinate in pixels
	 * @return the url of the current coordinate, or null, if there is
	 * nothing at (x, y), or it has no url set
	 */
	public String urlByCoordinate(int x, int y) {
		if (textController != null) {
			GPoint2D p = rectangle.getInversePoint(x - PADDING, y - PADDING);
			return textController.urlByCoordinate((int) p.getX(), (int) p.getY());
		}

		return "";
	}

	@Override
	public GRectangle getBounds() {
		return rectangle.getBounds();
	}

	@Override
	public RotatableBoundingBox getBoundingBox() {
		return rectangle.getBoundingBox();
	}

	@Override
	public double getWidthThreshold() {
		if (text.getHeight() - text.getMinHeight() < 2) {
			return text.getWidth();
		}

		return GeoInlineText.DEFAULT_WIDTH;
	}

	@Override
	public double getHeightThreshold() {
		return text.getMinHeight();
	}

	@Override
	public void draw(GGraphics2D g2) {
		if (text.isEuclidianVisible() && textController != null) {
			textController.draw(g2, rectangle.getDirectTransform());
		}
	}

	@Override
	public boolean hit(int x, int y, int hitThreshold) {
		return rectangle.hit(x, y);
	}

	@Override
	public boolean isInside(GRectangle rect) {
		return rect.contains(getBounds());
	}

	@Override
	public GeoElement getGeoElement() {
		return geo;
	}

	@Override
	public void remove() {
		if (textController != null) {
			textController.discard();
		}
	}

	@Override
	public void updateByBoundingBoxResize(GPoint2D point, EuclidianBoundingBoxHandler handler) {
		rectangle.updateByBoundingBoxResize(point, handler);
	}

	@Override
	public void fromPoints(ArrayList<GPoint2D> points) {
		rectangle.fromPoints(points);
	}

	@Override
	protected List<GPoint2D> toPoints() {
		return rectangle.toPoints();
	}

	/**
	 * @param key
	 *            formatting option
	 * @param val
	 *            value (String, int or bool, depending on key)
	 */
	@Override
	public void format(String key, Object val) {
		if (textController != null) {
			textController.format(key, val);
		}
	}

	@Override
	public <T> T getFormat(String key, T fallback) {
		if (textController != null) {
			return textController.getFormat(key, fallback);
		}
		return fallback;
	}

	@Override
	public String getHyperLinkURL() {
		if (textController != null) {
			return textController.getHyperLinkURL();
		}
		return "";
	}

	@Override
	public void setHyperlinkUrl(String url) {
		if (textController != null) {
			textController.setHyperlinkUrl(url);
		}
	}

	@Override
	public String getHyperlinkRangeText() {
		if (textController != null) {
			return textController.getHyperlinkRangeText();
		}

		return "";
	}

	@Override
	public void insertHyperlink(String url, String text) {
		if (textController != null) {
			textController.insertHyperlink(url, text);
		}
	}

	@Override
	public void switchListTo(String listType) {
		if (textController != null) {
			textController.switchListTo(listType);
		}
	}

	@Override
	public String getListStyle() {
		if (textController != null) {
			return textController.getListStyle();
		}
		return "";
	}

	@Override
	public BoundingBox<? extends GShape> getSelectionBoundingBox() {
		return getBoundingBox();
	}

	public InlineTextController getTextController() {
		return textController;
	}

	/**
	 * Setter to mock Carota.
	 * Nicer solutions are welcome.
	 *
	 * @param textController to set.
	 */
	public void setTextController(InlineTextController textController) {
		this.textController = textController;
	}
}
