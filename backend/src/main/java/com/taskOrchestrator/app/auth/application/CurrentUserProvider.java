package com.taskOrchestrator.app.auth.application;

//introduce a small interface in domain/application layer and hide Spring Security behind it
//Pure abstraction, Easy to mock in tests, no spring imports
//application layer depends on this not on security internals.
public interface CurrentUserProvider {
    CurrentUser getCurrentUser();
}