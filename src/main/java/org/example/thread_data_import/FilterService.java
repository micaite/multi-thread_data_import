package org.example.thread_data_import;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FilterService {

    public List<Person> filterByText (List<Person> data, String text){
        if (text == null || text.isBlank()) return data;

        String lower = text.toLowerCase();
        return data.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(lower)
                    || p.getLastName().toLowerCase().contains(lower)
                    || p.getEmail().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Person> filterByDateRange (List<Person> data, LocalDate from, LocalDate to){

        if (from == null && to == null) return data;

        return data.stream()
                .filter(p ->{
                    LocalDate date = p.getBirthDateParsed();
                    if (date == null) return false;
                    if (from != null && date.isBefore(from)) return false;
                    if (to != null && date.isAfter(to)) return false;
                    return true;
                }).collect(Collectors.toList());
    }

    public List<Person>  sortByField (List<Person> data, String field, boolean ascending) {

        Comparator<Person> comparator = resolveComparator (field);
        if (comparator == null) return data;

        if (!ascending) comparator = comparator.reversed();

        return data.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Person>  sortById (List<Person> data, boolean ascending) {

        Comparator<Person> comparator = Comparator.comparingInt(Person :: getIdInt);

        if (!ascending) comparator = comparator.reversed();

        return data.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public List<Person> apply (List<Person> data, String searchText, LocalDate from, LocalDate to, String sortField, boolean ascending) {

        List<Person> result = filterByText(data, searchText);
        result = filterByDateRange(result, from, to);

        if (sortField != null) {
            if (sortField.equals("ID")){
                result = sortById(result, ascending);
            } else {
                result = sortByField(result, sortField, ascending);
            }
        }

        return result;
    }

    private Comparator<Person> resolveComparator (String field) {
        if (field == null) return null;


        switch (field) {
            case "First Name":
                return Comparator.comparing(Person::getFirstName, String.CASE_INSENSITIVE_ORDER);
            case "Last Name":
                return Comparator.comparing(Person::getLastName, String.CASE_INSENSITIVE_ORDER);
            case "Email":
                return Comparator.comparing(Person::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "Country":
                return Comparator.comparing(Person::getCountry, String.CASE_INSENSITIVE_ORDER);
            case "Birth Date":
                return Comparator.comparing(p -> {
                    LocalDate d = ((Person) p).getBirthDateParsed();
                    return d != null ? d : LocalDate.MIN;
                });
            default:
                return null;
        }
    }

}
