package steps.system;

import io.cucumber.java.ParameterType;
import lombok.extern.slf4j.Slf4j;
import ru.sber.cb.ekp.utils.Comparator;

import java.util.Optional;

import static ru.sber.cb.ekp.utils.Context.getSmartContext;

@Slf4j
public class UtilsParam {
    @ParameterType("-?\\d+L?|-?\\d+\\.\\d+|\".*?\"|<\\w+>|null|false|true|[A-Z][\\w\\.]")
    public Optional<Object> context(String key) {
        return getSmartContext(key);
    }


    @ParameterType("<\\w+?>")
    public String context_key(String key) {
        return key;
    }

    @ParameterType("==|!=|=~|!~|содержит|не содержит")
    public Comparator.ComparatorEnum comparator(String chars) {
        return switch (chars) {
            case "==" -> Comparator.ComparatorEnum.EQUALS;
            case "!=" -> Comparator.ComparatorEnum.NOT_EQUALS;
            case "=~" -> Comparator.ComparatorEnum.LIKE;
            case "!~" -> Comparator.ComparatorEnum.NOT_LIKE;
            case "содержит" -> Comparator.ComparatorEnum.CONTAINS;
            case "не содержит" -> Comparator.ComparatorEnum.NOT_CONTAINS;
            default -> null;
        };
    }

    @ParameterType("\\d+|all|any")
    public String arr_idx(String key) {
        return key;
    }

}
