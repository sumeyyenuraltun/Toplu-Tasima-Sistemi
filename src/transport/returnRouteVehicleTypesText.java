package transport;

import org.graphstream.graph.Node;
import tasimaProject.Bus;
import tasimaProject.Tram;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class returnRouteVehicleTypesText {

    //Rotada hangi araçların kullanıldığı bilgisini döndüren fonksiyon
    //Rotada kullanılan "Sadece Otobüs" veya "Otobüs ve Tramvay" gibi bilgileri iletmek için


    private String routeVehicleInfoText;

    public String returnRouteVehicleTypesText(List<Node> Route){



        Set<Class<?>> routeVehicleInfo = new HashSet<>();
        routeVehicleInfo = returnRouteVehicleInfo(Route);

        String turString;
        if(routeVehicleInfo.size()>=2)
            turString = "Otobüs ve Tramvay";
        else if(routeVehicleInfo.contains(Bus.class))
            turString = "Sadece Otobüs";
        else if(routeVehicleInfo.contains(Tram.class))
            turString = "Sadece Tramvay";
        else
            turString = "Ulaşım Aracı Bilgisi Bulunamadı";

        this.routeVehicleInfoText = turString;

        return routeVehicleInfoText;

    }


    public Set<Class<?>> returnRouteVehicleInfo(List<Node> Route) {
        Set<Class<?>> routeVehicleInfo = new HashSet<>();
        for (Node durak : Route) {
            Object vehicle = durak.getAttribute("Vehicle");
            if (vehicle != null) {
                routeVehicleInfo.add(vehicle.getClass());
            } else {

                System.out.println("Vehicle attribute is null for node: " + durak);
            }
        }
        return routeVehicleInfo;
    }
}
