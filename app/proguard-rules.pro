# R8 release rules. Retrofit, OkHttp, Gson and Koin ship consumer rules; DTO keeps live in
# core/network/consumer-rules.pro.

# Randomized obfuscation: every class, method, field and package is renamed from this dictionary.
-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt
-repackageclasses ''
-allowaccessmodification

# Keep line numbers in crash reports; mapping.txt de-obfuscates them.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
