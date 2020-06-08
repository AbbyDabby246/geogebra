package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.RotatableBoundingBox;
import org.geogebra.common.euclidian.inline.InlineTableController;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoInlineTable;

public class DrawInlineTable extends Drawable implements DrawInline, HasFormat {

	private final InlineTableController tableController;

	private final TransformableRectangle rectangle;

	/**
	 * @param view view
	 * @param table editable table
	 */
	public DrawInlineTable(EuclidianView view, GeoInlineTable table) {
		super(view, table);
		tableController = view.getApplication().createTableController(view, table);
		rectangle = new TransformableRectangle(view, table);
		update();
	}

	@Override
	public void update() {
		rectangle.updateSelfAndBoundingBox();

		if (tableController != null) {
			tableController.update();
		}
	}

	@Override
	public void draw(GGraphics2D g2) {
		if (geo.isEuclidianVisible() && tableController != null) {
			tableController.draw(g2, rectangle.getDirectTransform());
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
	public GRectangle getBounds() {
		return rectangle.getBounds();
	}

	@Override
	public RotatableBoundingBox getBoundingBox() {
		return rectangle.getBoundingBox();
	}

	@Override
	public GeoElement getGeoElement() {
		return geo;
	}

	@Override
	public void remove() {
		tableController.removeFromDom();
	}

	public boolean isInEditMode() {
		return tableController != null && tableController.isInEditMode();
	}

	@Override
	public void format(String key, Object val) {
		tableController.format(key, val);
	}

	@Override
	public <T> T getFormat(String key, T fallback) {
		return tableController.getFormat(key, fallback);
	}

	@Override
	public String getHyperLinkURL() {
		return "";
	}

	@Override
	public void setHyperlinkUrl(String url) {
		// intentionally empty - for now
	}

	@Override
	public String getHyperlinkRangeText() {
		return "";
	}

	@Override
	public void insertHyperlink(String url, String text) {
		// intentionally empty - for now
	}

	@Override
	public String getListStyle() {
		return null;
	}

	@Override
	public void switchListTo(String listType) {
		// intentionally empty - for now
	}

	@Override
	public void updateContent() {
		if (tableController != null) {
			tableController.updateContent();
		}
	}

	@Override
	public void toBackground() {
		if (tableController != null) {
			tableController.toBackground();
		}
	}

	@Override
	public void toForeground(int x, int y) {
		if (tableController != null) {
			GPoint2D p = rectangle.getInversePoint(x, y);
			tableController.toForeground((int) p.getX(), (int) p.getY());
		}
	}
}
