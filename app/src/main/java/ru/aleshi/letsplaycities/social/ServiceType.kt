package ru.aleshi.letsplaycities.social

enum class ServiceType constructor(val network: ISocialNetwork) {
    NV(NativeAccess()),
    OK(OdnoklassnikiSN()),
    VK(VKontakte()),
    FB(Facebook()),
    GL(Google());
}