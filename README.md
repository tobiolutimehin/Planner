# Planner

Planner is an Android app that helps you manage your todo list and keep track of your flights. Stay
organized and never miss an important task or flight again.

[![API](https://img.shields.io/badge/API-31%2B-green)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.7.xxx-brightgreen)](https://kotlinlang.org)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-blueviolet)

## Summary

Planner is a feature-rich productivity app designed to streamline your task management and flight
tracking experience. Whether you're a busy professional or a frequent traveler, Planner provides the
tools you need to stay organized, prioritize your tasks, and keep track of your flights—all in one
convenient app.

## Features

- **Todo List Management**: Create, edit, and delete todo items with ease. Organize your tasks into
  categories and set priorities to stay focused and efficient.

- **Flight Tracking**: View your upcoming flights at a glance. Access detailed flight information
  including departure and arrival times, airline details, and more.

- **User-Friendly Interface**: Enjoy a clean and intuitive user interface that makes it easy to
  navigate through your tasks and flights. Effortlessly manage your schedule with Planner's
  user-friendly design.

## Technologies and Libraries Used

- Kotlin programming language: A modern and expressive language that offers concise and safe code
  syntax.

- Android Jetpack libraries: Leveraging the power of Android Jetpack, Planner utilizes several core
  libraries including:

    - Room: A SQLite object-mapping library that provides an abstraction layer for seamless database
      operations.
    - Navigation: A powerful library for implementing in-app navigation, making it easy to navigate
      between different screens and fragments.
    - ViewModel: A library that provides a lifecycle-aware way to store and manage UI-related data.
    - LiveData: A library that provides lifecycle-aware observable data holders, allowing data to be
      updated automatically in the UI.
    - Data Binding: A library that allows you to bind UI components in your layouts to data sources
      in your app using a declarative format.
    - Android KTX: A set of Kotlin extensions that are compatible with all Android libraries and
      frameworks.
    - Test: A library that provides testing APIs for unit and integration testing.
    - AndroidX: A major improvement to the original Android Support Library, offering backward
      compatibility and other enhancements.
    - Material Design Components: A library that provides a collection of UI components based on
      Material Design guidelines.

## Architecture

The Planner app follows the MVVM (Model-View-ViewModel) architecture pattern. This architectural
approach provides clear separation of concerns, making the codebase modular and maintainable. The
key components of the architecture are:

- **Model**: Represents the data and business logic of the app. It includes data models,
  repositories, and data sources. The Model component interacts with local and remote data sources
  to provide the necessary data to the ViewModel.

- **View**: Responsible for displaying the user interface to the app's users. It includes
  activities, fragments, and layout files. The View communicates with the ViewModel to retrieve data
  and update the UI.

- **ViewModel**: Acts as a mediator between the Model and View components. It holds the UI-related
  data and provides data to the View. The ViewModel receives user interactions from the View and
  communicates with the Model to perform data operations.

The MVVM architecture enhances code maintainability, testability, and scalability. It also enables
better separation of concerns, allowing developers to work on different parts of the app
independently.

## License