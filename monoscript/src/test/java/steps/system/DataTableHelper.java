package steps.system;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class DataTableHelper {


    public static DataTable DTof(String... entries) {
        List<List<String>> result = new ArrayList<>();
        List<String> tempList = new ArrayList<>(2);

        Stream.of(entries).forEach(str -> {
            tempList.add(str);
            if (tempList.size() == 2) { // Собираем по два элемента
                result.add(new ArrayList<>(tempList));
                tempList.clear();
            }
        });

        return DataTable.create(result, new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.ENGLISH)));
    }
}