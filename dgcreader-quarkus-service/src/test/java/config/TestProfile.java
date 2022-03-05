package config;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TestProfile implements QuarkusTestProfile {

    @Override
    public boolean disableApplicationLifecycleObservers() {
        return true;
    }
}
