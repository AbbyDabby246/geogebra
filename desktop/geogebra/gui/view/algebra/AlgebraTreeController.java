package geogebra.gui.view.algebra;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import geogebra.common.euclidian.event.AbstractEvent;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.gui.GuiManager;
import geogebra.main.Application;

/**
 * Controller for tree of geos
 * @author mathieu
 *
 */
public class AlgebraTreeController extends geogebra.common.gui.view.algebra.AbstractAlgebraController
implements MouseListener, MouseMotionListener{

	

	/** tree */
	private AlgebraTree tree;

	
	/** Creator  
	 * @param kernel kernel
	 * */
	public AlgebraTreeController(Kernel kernel) {
		super(kernel);
	}
	

	/**
	 * set the tree controlled
	 * @param tree tree
	 */
	public void setTree(AlgebraTree tree){
		this.tree = tree;
	}


	/**
	 * check double click
	 * @param geo geo clicked
	 * @param e mouse event
	 * @return true if double click
	 */
	protected boolean checkDoubleClick(GeoElement geo, MouseEvent e){

		return false;
	}

	/*
	 * MouseListener implementation for popup menus
	 */

	public void mouseClicked(java.awt.event.MouseEvent e) {	
		// right click is consumed in mousePressed, but in GeoGebra 3D,
		// where heavyweight popup menus are enabled this doesn't work
		// so make sure that this is no right click as well (ticket #302)
		if (e.isConsumed() || Application.isRightClick(e)) {
			return;
		}

		// get GeoElement at mouse location		
		TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
		GeoElement geo = AlgebraTree.getGeoElementForPath(tp);	
		
		ArrayList<GeoElement> groupedGeos = null;

		// check if we clicked on the 16x16 show/hide icon
		if (geo != null) {
			Rectangle rect = tree.getPathBounds(tp);		
			boolean iconClicked = rect != null && e.getX() - rect.x < 16; // distance from left border				
			if (iconClicked) {
				// icon clicked: toggle show/hide
				geo.setEuclidianVisible(!geo.isSetEuclidianVisible());
				geo.updateVisualStyle();
				app.storeUndoInfo();
				kernel.notifyRepaint();
				return;
			}	
		
		}else{ // try group action
			groupedGeos = groupAction(e,tp,false);

		}
		

		// check double click
		if (checkDoubleClick(geo, e))
			return;

		EuclidianViewInterfaceCommon ev = app.getActiveEuclidianView();
		int mode = ev.getMode();
		if (!skipSelection && (mode == EuclidianConstants.MODE_MOVE || mode == EuclidianConstants.MODE_RECORD_TO_SPREADSHEET) ) {
			// update selection	
			if (geo == null){
				if (!Application.isControlDown(e) && !e.isShiftDown())
					app.clearSelectedGeos();
				
				if (groupedGeos!=null)
					app.addSelectedGeos(groupedGeos, true);
					
			}else {					
				// handle selecting geo
				if (Application.isControlDown(e)) {
					app.toggleSelectedGeo(geo); 													
					if (app.getSelectedGeos().contains(geo)) lastSelectedGeo = geo;
				} else if (e.isShiftDown() && lastSelectedGeo != null) {
					boolean nowSelecting = true;
					boolean selecting = false;
					boolean aux = geo.isAuxiliaryObject();
					boolean ind = geo.isIndependent();
					boolean aux2 = lastSelectedGeo.isAuxiliaryObject();
					boolean ind2 = lastSelectedGeo.isIndependent();

					if ((aux == aux2 && aux == true) || (aux == aux2 && ind == ind2)) {

						Iterator<GeoElement> it = kernel.getConstruction().getGeoSetLabelOrder().iterator();

						boolean direction = geo.getLabel(StringTemplate.defaultTemplate).
								compareTo(lastSelectedGeo.getLabel(StringTemplate.defaultTemplate)) < 0;

						while (it.hasNext()) {
							GeoElement geo2 = it.next();
							if ((geo2.isAuxiliaryObject() == aux && aux == true)
									|| (geo2.isAuxiliaryObject() == aux && geo2.isIndependent() == ind)) {

								if (direction && geo2 == lastSelectedGeo) selecting = !selecting;
								if (!direction && geo2 == geo) selecting = !selecting;

								if (selecting) {
									app.toggleSelectedGeo(geo2);
									nowSelecting = app.getSelectedGeos().contains(geo2);
								}

								if (!direction && geo2 == lastSelectedGeo) selecting = !selecting;
								if (direction && geo2 == geo) selecting = !selecting;
							}
						}
					}

					if (nowSelecting) {
						app.addSelectedGeo(geo); 
						lastSelectedGeo = geo;
					} else {
						app.removeSelectedGeo(lastSelectedGeo);
						lastSelectedGeo = null;
					}

				} else {							
					app.clearSelectedGeos(false); //repaint will be done next step
					app.addSelectedGeo(geo);
					lastSelectedGeo = geo;
				}
			}
		} 
		else if (mode != EuclidianConstants.MODE_SELECTION_LISTENER) {
			// let euclidianView know about the click
			AbstractEvent event = geogebra.euclidian.event.MouseEvent.wrapEvent(e);
			ev.clickedGeo(geo, event);
			event.release();
		} else 
			// tell selection listener about click
			app.geoElementSelected(geo, false);


		// Alt click: copy definition to input field
		if (geo != null && e.isAltDown() && app.showAlgebraInput()) {			
			// F3 key: copy definition to input bar
			app.getGlobalKeyDispatcher().handleFunctionKeyForAlgebraInput(3, geo);			
		}

		ev.mouseMovedOver(null);		
	}
	
	/**
	 * cancel editing in the view
	 */
	protected void viewCancelEditing(){
		//nothing to do here
	}

	public void mousePressed(java.awt.event.MouseEvent e) {
		viewCancelEditing();
		
		geogebra.common.awt.Point mouseCoords = new geogebra.common.awt.Point(e.getPoint().x,e.getPoint().y);

		boolean rightClick = app.isRightClickEnabled() && Application.isRightClick(e);
		
		
		// RIGHT CLICK
		if (rightClick) {
			e.consume();

			// get GeoElement at mouse location		
			TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
			GeoElement geo = AlgebraTree.getGeoElementForPath(tp);

			if (geo != null && !app.containsSelectedGeo(geo)) {
				app.clearSelectedGeos();					
			}

			// single selection: popup menu
			if (app.selectedGeosSize() < 2) {				
				if(geo == null) {
					AlgebraContextMenu contextMenu = new AlgebraContextMenu((Application)app);
					contextMenu.show(tree, e.getPoint().x, e.getPoint().y);
				} else {
					ArrayList<GeoElement> temp = new ArrayList<GeoElement>();
					temp.add(geo);
					((GuiManager)app.getGuiManager()).showPopupMenu(temp, tree, mouseCoords);
				}
			} 
			// multiple selection: popup menu (several geos)
			else {
				if(geo != null) {
					((GuiManager)app.getGuiManager()).showPopupMenu(app.getSelectedGeos(), tree, mouseCoords);
				}
			}	

			// LEFT CLICK	
		} else {

			// When a single, new selection is made with no key modifiers
			// we need to handle selection in mousePressed, not mouseClicked.
			// By doing this selection early, a DnD drag will come afterwards
			// and grab the new selection. 
			// All other selection types must be handled later in mouseClicked. 
			// In this case a DnD drag starts first and grabs the previously selected 
			// geos (e.g. cntrl-selected or EV selected) as the user expects.

			skipSelection = false; // flag to prevent duplicate selection in MouseClicked

			TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
			GeoElement geo = AlgebraTree.getGeoElementForPath(tp);	
			EuclidianViewInterfaceCommon ev = app.getActiveEuclidianView();
			int mode = ev.getMode();

			if ( (mode == EuclidianConstants.MODE_MOVE || mode == EuclidianConstants.MODE_SELECTION_LISTENER)  && 
					!Application.isControlDown(e) && !e.isShiftDown())
			{
				if( geo != null  && !app.containsSelectedGeo(geo)) 
				{					
					app.clearSelectedGeos(false); //repaint will be done next step
					app.addSelectedGeo(geo);
					lastSelectedGeo = geo;
					skipSelection = true;
				}else{
					ArrayList<GeoElement> groupedGeos = groupAction(e,tp,true);
					if (groupedGeos!=null && !app.containsSelectedGeos(groupedGeos)){
						app.clearSelectedGeos(false); //repaint will be done next step
						app.addSelectedGeos(groupedGeos, true);
						skipSelection = true;
					}
				}
			}

		}
	}
	


	private ArrayList<GeoElement> groupAction(MouseEvent e, TreePath tp, boolean mousePressed){
		
		Rectangle rect = tree.getPathBounds(tp);		
		if (rect!=null){ //group action
			if (e.getX()-rect.x<16){ // collapse/expand icon
				if (mousePressed){
					if (tree.isCollapsed(tp))
						tree.expandPath(tp);
					else
						tree.collapsePath(tp);
				}
			}else{ // collect geos of the group
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();					
				ArrayList<GeoElement> groupedGeos = new ArrayList<GeoElement>();
				for (int i=0; i<node.getChildCount(); i++){
					groupedGeos.add((GeoElement) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
				}
				return groupedGeos;
			}
		}
		
		return null;
		
	}

	public void mouseReleased(java.awt.event.MouseEvent e) {
		//
	}

	public void mouseEntered(java.awt.event.MouseEvent p1) {
		//
	}
	

	public void mouseExited(java.awt.event.MouseEvent p1) {		
		//
	}

	// MOUSE MOTION LISTENER
	public void mouseDragged(MouseEvent arg0) {
		//
	}
	
	/**
	 * 
	 * @return true if view is editing
	 */
	protected boolean viewIsEditing(){
		return false;
	}

	// tell EuclidianView
	public void mouseMoved(MouseEvent e) {		
		if (viewIsEditing())
			return;

		int x = e.getX();
		int y = e.getY();

		GeoElement geo = AlgebraTree.getGeoElementForLocation(tree, x, y);

		// tell EuclidianView to handle mouse over
		//EuclidianView ev = app.getEuclidianView();
		EuclidianViewInterfaceCommon ev = app.getActiveEuclidianView();
		ev.mouseMovedOver(geo);								

		if (geo != null) {
			app.setTooltipFlag();
			tree.setToolTipText(geo.getLongDescriptionHTML(true, true));
			app.clearTooltipFlag();
		} else{
			tree.setToolTipText(null);	
			TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
			if (!tree.isCollapsed(tp)){
				Rectangle rect = tree.getPathBounds(tp);		
				if (rect!=null){ //mouse over group
					if (e.getX()-rect.x>16){ // collect geos of the group
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
						ArrayList<GeoElement> groupedGeos = new ArrayList<GeoElement>();
						for (int i=0; i<node.getChildCount(); i++){
							groupedGeos.add((GeoElement) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
						}
						ev.mouseMovedOverList(groupedGeos);
					}
				}
			}
			
		}
	}
	
	
	
}
