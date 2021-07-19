package info.kgeorgiy.ja.korobejnikov.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    private final Comparator<Student> idComparator = Comparator.comparing(Student::getId);

    private final Comparator<Student> studentNameComparator =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .reversed()
                    .thenComparing(Student::getId);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapAndCollectToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapAndCollectToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapAndCollectToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapAndCollectToList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(idComparator).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortToList(students, idComparator);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortToList(students, studentNameComparator);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterSort(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterSort(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterSort(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filterByTarget(students, Student::getGroup, group)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareToIgnoreCase)));
    }

    private <T> List<Student> filterSort(Collection<Student> elements, Function<Student, T> getter, T target) {
        return filterByTarget(elements, getter, target)
                .sorted(studentNameComparator)
                .collect(Collectors.toList());
    }

    private List<Student> sortToList(Collection<Student> elements, Comparator<Student> comparator) {
        return elements.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private <T> Stream<Student> filterByTarget(Collection<Student> elements, Function<Student, T> getter, T target) {
        return elements.stream()
                .filter(element -> getter.apply(element).equals(target));
    }

    private <T> List<T> mapAndCollectToList(Collection<Student> elements, Function<Student, T> function) {
        return elements.stream()
                .map(function)
                .collect(Collectors.toList());
    }


}


