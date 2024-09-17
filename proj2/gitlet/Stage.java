package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Stage implements Serializable {
    private Map<String, String> pathToBlobID = new HashMap<>();

}
