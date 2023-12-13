package store_api;

import store_api.human.Employee;

@FunctionalInterface
public interface Removable {
    public abstract void remove(Employee employee);
}
