# Models
-keep class co.thingthing.fleksyapps.mediashare.models.** { *; }

# Public api
-keep class co.thingthing.fleksyapps.mediashare.MediaShareApp { *; }

-keep,allowobfuscation,allowshrinking class io.reactivex.Flowable
-keep,allowobfuscation,allowshrinking class io.reactivex.Maybe
-keep,allowobfuscation,allowshrinking class io.reactivex.Observable
-keep,allowobfuscation,allowshrinking class io.reactivex.Single
