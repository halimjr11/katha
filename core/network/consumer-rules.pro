# Gson builds DTOs by reflection. Names may be obfuscated (the JSON key is in @SerializedName),
# but the class, its @SerializedName fields and the no-arg constructor must survive shrinking.
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

-keep,allowobfuscation class com.halimjr11.network.dto.** {
    <init>();
    @com.google.gson.annotations.SerializedName <fields>;
}
