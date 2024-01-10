package nl.tudelft.sem.template.example.domain.analytics;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

public class TestUserActionRepository implements UserActionRepository {

    private int id = -1;
    private final List<UserAction> actions = new ArrayList<>();

    @Override
    public long countByType(String type) {
        return actions.stream().filter(e -> e.getType().equals(type)).count();
    }

    @Override
    public List<Object[]> getActionsByTypeFrequency() {
        Map<String, Integer> map = new HashMap<>();
        for(UserAction action : actions) {
            if(map.containsKey(action.getType()))
                map.put(action.getType(), map.get(action.getType()) + 1);
            else map.put(action.getType(), 1);
        }

        List<Object[]> objects = new ArrayList<>();
        for(var entry : map.entrySet())
            objects.add(new Object[] {entry.getKey(), entry.getValue()});

        objects.sort((a, b) -> Integer.compare((Integer) b[1], (Integer) a[1]));
        return objects;
    }

    @Override
    public List<UserAction> findAll() {
        return new ArrayList<>(actions);
    }

    @Override
    public List<UserAction> findAll(Sort sort) {
        // Won't use
        return null;
    }

    @Override
    public Page<UserAction> findAll(Pageable pageable) {
        // Won't use
        return null;
    }

    @Override
    public List<UserAction> findAllById(Iterable<Integer> integers) {
        // Won't use
        return null;
    }

    @Override
    public long count() {
        return actions.size();
    }

    @Override
    public void deleteById(Integer integer) {
        // Won't use
    }

    @Override
    public void delete(UserAction entity) {
        // Won't use
    }

    @Override
    public void deleteAll(Iterable<? extends UserAction> entities) {
        // Won't use
    }

    @Override
    public void deleteAll() {
        // Won't use
    }

    @Override
    public <S extends UserAction> S save(S entity) {
        entity.setId(++id);
        actions.add(entity);
        return entity;
    }

    @Override
    public <S extends UserAction> List<S> saveAll(Iterable<S> entities) {
        // Won't use
        return null;
    }

    @Override
    public Optional<UserAction> findById(Integer integer) {
        return actions.stream().filter(e -> e.getId() == integer).findAny();
    }

    @Override
    public boolean existsById(Integer integer) {
        return findById(integer).isPresent();
    }

    @Override
    public void flush() {
        // Won't use
    }

    @Override
    public <S extends UserAction> S saveAndFlush(S entity) {
        // Won't use
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<UserAction> entities) {
        // Won't use
    }

    @Override
    public void deleteAllInBatch() {
        // Won't use
    }

    @Override
    public UserAction getOne(Integer integer) {
        // Won't use
        return null;
    }

    @Override
    public <S extends UserAction> Optional<S> findOne(Example<S> example) {
        // Won't use
        return Optional.empty();
    }

    @Override
    public <S extends UserAction> List<S> findAll(Example<S> example) {
        // Won't use
        return null;
    }

    @Override
    public <S extends UserAction> List<S> findAll(Example<S> example, Sort sort) {
        // Won't use
        return null;
    }

    @Override
    public <S extends UserAction> Page<S> findAll(Example<S> example, Pageable pageable) {
        // Won't use
        return null;
    }

    @Override
    public <S extends UserAction> long count(Example<S> example) {
        // Won't use
        return 0;
    }

    @Override
    public <S extends UserAction> boolean exists(Example<S> example) {
        // Won't use
        return false;
    }
}
