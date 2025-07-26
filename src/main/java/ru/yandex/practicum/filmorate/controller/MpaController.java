package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaDbStorage mpaDbStorage;

    @GetMapping
    public List<Mpa> getAllMpa() {
        return mpaDbStorage.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return mpaDbStorage.getMpaById(id);
    }
}