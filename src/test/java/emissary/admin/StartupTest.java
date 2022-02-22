package emissary.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import emissary.config.Configurator;
import emissary.directory.DirectoryPlace;
import emissary.directory.EmissaryNode;
import emissary.pickup.file.FilePickUpClient;
import emissary.pickup.file.FilePickUpPlace;
import emissary.place.CoordinationPlace;
import emissary.place.sample.DelayPlace;
import emissary.place.sample.DevNullPlace;
import emissary.test.core.UnitTest;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

public class StartupTest extends UnitTest {

    @Test
    public void testSortPlaces() throws IOException {
        List<String> somePlaces = new ArrayList<>();
        EmissaryNode node = new EmissaryNode();
        String location = createPlaceList(node, somePlaces);


        Startup startup = new Startup(node.getNodeConfigurator(), node);
        startup.sortPlaces(somePlaces);
        // TODO: figure out why the key isn't http://localhost:8001. Seems
        // this way when running too, so get("") works. This test is just testing the
        // PickUp stuff is pulled out separately
        List<String> pickups = startup.pickupLists.get("");
        List<String> places = startup.placeLists.get("");
        assertThat(pickups, containsInAnyOrder(location + "/" + "FilePickUpPlace", location + "/" + "FilePickUpClient"));
        assertThat(places, containsInAnyOrder(location + "/" + "CoordinationPlace", location + "/" + "DelayPlace", location + "/" + "DevNullPlace"));
    }

    @Test
    public void testParallelStartupPlaceSetup() throws IOException {
        List<String> somePlaces = new ArrayList<>();
        EmissaryNode node = new EmissaryNode();
        String location = createPlaceList(node, somePlaces);

        Configurator nodeConfigurator = node.getNodeConfigurator();
        nodeConfigurator.removeEntry("PARALELL_PLACE_STARTUP", "*");
        nodeConfigurator.addEntry("PARALELL_PLACE_STARTUP", "true");


        Startup startup = new Startup(nodeConfigurator, node);
        startup.placesToStart.addAll(somePlaces);

        Map<String, String> localDirectories = new HashedMap<>();
        localDirectories.put("", location + "/" + DirectoryPlace.class.getSimpleName());
        Map<String, String> places = new HashedMap<>();
        startup.runPlaceSetup(Startup.DIRECTORYADD, localDirectories, places, somePlaces);

        assertThat(places.values(), containsInAnyOrder(location + "/" + "CoordinationPlace", location + "/" + "DelayPlace",
                location + "/" + "DevNullPlace", location + "/" + "FilePickUpPlace", location + "/" + "FilePickUpClient"));
    }

    private String createPlaceList(EmissaryNode node, List<String> somePlaces) {
        String location = "http://" + node.getNodeName() + ":" + node.getNodePort();
        somePlaces.add(location + "/" + CoordinationPlace.class.getSimpleName());
        somePlaces.add(location + "/" + DelayPlace.class.getSimpleName());
        somePlaces.add(location + "/" + DevNullPlace.class.getSimpleName());
        somePlaces.add(location + "/" + FilePickUpPlace.class.getSimpleName());
        somePlaces.add(location + "/" + FilePickUpClient.class.getSimpleName());
        return location;
    }
}
