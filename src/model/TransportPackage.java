package model;

import java.util.ArrayList;
import java.util.List;

public class TransportPackage {

    private Factory producerFactory;
    private Factory consumerFactory;
    private String commodity;
    private int amount;
    private List<Station> path = new ArrayList<>();

    public TransportPackage(Factory producerFactory, Factory consumerFactory, String commodity, int amount) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
        this.commodity = commodity;
        this.amount = amount;
    }

    public Factory getProducerFactory() {
        return producerFactory;
    }

    public void setProducerFactory(Factory producerFactory) {
        this.producerFactory = producerFactory;
    }

    public Factory getConsumerFactory() {
        return consumerFactory;
    }

    public void setConsumerFactory(Factory consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public List<Station> getPath() {
        return path;
    }

    public void setPath(List<Station> path) {
        this.path = path;
    }

    public Station getNextStationForTransport() {
        return path.get(0);
    }

    @Override
    public String toString() {
        path.forEach((x) -> System.out.print(" ID "+x.getId()));
        return "TransportPackage{" +
                "producerFactory=" + producerFactory +
                ", consumerFactory=" + consumerFactory +
                ", commodity='" + commodity + '\'' +
                ", amount=" + amount +
                ", path=" + path+
                '}';


    }
}


