public interface CommunicationStrategy {
    void connect(String host, int port) throws Exception;
    void send(String message) throws Exception;
    void disconnect() throws Exception;
    String getName();
}

abstract class StrategyFactory {
    public abstract CommunicationStrategy createStrategy();
}