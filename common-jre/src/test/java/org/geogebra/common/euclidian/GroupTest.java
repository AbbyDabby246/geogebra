package org.geogebra.common.euclidian;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geogebra.common.factories.AwtFactoryCommon;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.jre.headless.LocalizationCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.geos.groups.Group;
import org.geogebra.common.util.InternalClipboard;
import org.junit.Before;
import org.junit.Test;

public class GroupTest {
	private Construction construction;
	private AppCommon app;

	@Before
	public void setUp() {
		AwtFactoryCommon factoryCommon = new AwtFactoryCommon();
		app = new AppCommon(new LocalizationCommon(2), factoryCommon) {
			@Override
			public boolean isWhiteboardActive(){
				return true;
			}
		};
		construction = app.getKernel().getConstruction();
	}

	@Test
	public void testGeosNotGrupped() {
		assertFalse(Group.isInSameGroup(withGivenNumberOfGeos(2)));
	}

	private ArrayList<GeoElement> withGivenNumberOfGeos(int count) {
		ArrayList<GeoElement> geos = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			GeoPolygon polygon = new GeoPolygon(construction);
			polygon.setLabel("label" + i);
			geos.add(polygon);
		}
		return geos;
	}

	@Test
	public void testCreateGroup() {
		ArrayList<GeoElement> geos = withGivenNumberOfGeos(3);
		construction.createGroup(geos);
		assertTrue(Group.isInSameGroup(geos));
	}

	@Test
	public void testCreateTwoDifferentGroups() {
		ArrayList<GeoElement> geos1 = withGivenNumberOfGeos(3);
		ArrayList<GeoElement> geos2 = withGivenNumberOfGeos(5);
		construction.createGroup(geos1);
		construction.createGroup(geos2);
		geos1.addAll(geos2);
		assertFalse(Group.isInSameGroup(geos1));
	}

	@Test
	public void testGrouppedGeos() {
		ArrayList<GeoElement> geos = withGivenNumberOfGeos(5);
		Group group = new Group(geos);
		List<GeoElement> result = group.getGroupedGeos();
		assertEquals(geos, result);
	}

	@Test
	public void testRemoveGeoRemovesGroup() {
		ArrayList<GeoElement> geos1 = withGivenNumberOfGeos(2);
		ArrayList<GeoElement> geos2 = withGivenNumberOfGeos(2);
		construction.createGroup(geos1);
		construction.createGroup(geos2);
		geos1.get(0).remove();
		assertEquals(1, construction.getGroups().size());
	}

	@Test
	public void testCopyPasteGroup() {
		ArrayList<GeoElement> geos = new ArrayList<>();
		GeoPoint A = new GeoPoint(construction, "A", 0, 0, 1);
		GeoPoint B = new GeoPoint(construction, "B", 3, 0, 1);
		geos.add(A);
		geos.add(B);
		construction.createGroup(geos);
		app.getSelectionManager().setSelectedGeos(geos);
		InternalClipboard.duplicate(app, app.getSelectionManager().getSelectedGeos());
		String label1Group1Geo = construction.getGroups().get(0).getGroupedGeos().get(0).getLabelSimple();
		String label2Group1Geo = construction.getGroups().get(1).getGroupedGeos().get(0).getLabelSimple();
		String label1Group2Geo = construction.getGroups().get(0).getGroupedGeos().get(1).getLabelSimple();
		String label2Group2Geo = construction.getGroups().get(1).getGroupedGeos().get(1).getLabelSimple();
		assertThat(construction.getGroups().size(), equalTo(2));
		assertThat(construction.getGroups().get(0).getGroupedGeos().size() , equalTo(2));
		assertThat(construction.getGroups().get(1).getGroupedGeos().size() , equalTo(2));
		assertThat(label2Group1Geo.substring(0, 1), equalTo(label1Group1Geo));
		assertThat(label2Group2Geo.substring(0, 1), equalTo(label1Group2Geo));
	}

	@Test
	public void copyGroupShouldMaintainLayers(){
		ArrayList<GeoElement> geos = new ArrayList<>();
		GeoElement A = new GeoPoint(construction, "A", 0, 0, 1);
		GeoPoint B = new GeoPoint(construction, "B", 3, 0, 1);
		geos.add(A);
		geos.add(B);
		construction.getLayerManager().moveForward(Collections.singletonList(A));
		construction.createGroup(geos);
		assertEquals(0, lookup("B").getOrdering());
		assertEquals(1, lookup("A").getOrdering());
		InternalClipboard.duplicate(app, geos);
		assertEquals(0, lookup("B").getOrdering());
		assertEquals(1, lookup("A").getOrdering());
		assertEquals(2, lookup("B_1").getOrdering());
		assertEquals(3, lookup("A_1").getOrdering());
	}

	private GeoElement lookup(String label) {
		return app.getKernel().lookupLabel(label);
	}
}
