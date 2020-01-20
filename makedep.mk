define get_jar
printf "get $(1)\n"; \
wget -P dep/ $(1) >> wget.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat wget.log; \
fi; \
exit $$RES
endef

define get_zip
printf "get $(1)\n"; \
wget -P dep/archive/ $(1) >> wget.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat wget.log; \
fi; \
exit $$RES
endef

define get_tar
printf "get $(1)\n"; \
wget -P dep/archive/ $(1) >> wget.log 2>&1; \
RES=$$?; \
if [ $$RES -ne 0 ]; then \
	cat wget.log; \
fi; \
exit $$RES
endef

.PHONY : all _prep _cleanup

JFX=\
dep/javafx.base.jar \
dep/javafx.controls.jar \
dep/javafx.fxml.jar \
dep/javafx.graphics.jar \
dep/javafx.media.jar \
dep/javafx.swing.jar \
dep/javafx-swt.jar \
dep/javafx.web.jar \
dep/libavplugin-54.so \
dep/libavplugin-56.so \
dep/libavplugin-57.so \
dep/libavplugin-ffmpeg-56.so \
dep/libavplugin-ffmpeg-57.so \
dep/libavplugin-ffmpeg-58.so \
dep/libdecora_sse.so \
dep/libfxplugins.so \
dep/libglassgtk2.so \
dep/libglassgtk3.so \
dep/libglass.so \
dep/libgstreamer-lite.so \
dep/libjavafx_font_freetype.so \
dep/libjavafx_font_pango.so \
dep/libjavafx_font.so \
dep/libjavafx_iio.so \
dep/libjfxmedia.so \
dep/libjfxwebkit.so \
dep/libprism_common.so \
dep/libprism_es2.so \
dep/libprism_sw.so

all : \
_prep \
dep/commons-dbcp2-2.7.0.jar \
dep/commons-logging-1.2.jar \
dep/commons-pool2-2.7.0.jar \
dep/gtree-2.0.jar \
dep/libgtree.so \
dep/com-sun-tools-visualvm-charts-RELEASE139.jar \
dep/com-sun-tools-visualvm-uisupport-RELEASE139.jar \
dep/org-netbeans-lib-profiler-ui-RELEASE139.jar \
dep/org-netbeans-lib-profiler-charts-RELEASE139.jar \
dep/org-netbeans-modules-profiler-api-RELEASE139.jar \
dep/org-openide-util-lookup-RELEASE139.jar \
$(JFX)

_prep :
	@rm -f wget.log
	@mkdir -p dep/archive

####### Core Dependencies ######################################################

dep/commons-dbcp2-2.7.0.jar :
	@$(call get_jar,https://repo1.maven.org/maven2/org/apache/commons/commons-dbcp2/2.7.0/commons-dbcp2-2.7.0.jar)

dep/commons-logging-1.2.jar :
	@$(call get_jar,https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar)

dep/commons-pool2-2.7.0.jar :
	@$(call get_jar,https://repo1.maven.org/maven2/org/apache/commons/commons-pool2/2.7.0/commons-pool2-2.7.0.jar)

dep/gtree-2.0.jar : | dep/gtree-2.0-linux
	@printf "symlink gtree-2.0.jar\n"
	@ln -s -t dep/ gtree-2.0-linux/$(@F)

dep/libgtree.so : | dep/gtree-2.0-linux
	@printf "symlink libgtree.so\n"
	@ln -s -t dep/ gtree-2.0-linux/$(@F)

dep/gtree-2.0-linux : | dep/archive/gtree-2.0-linux.tar
	@printf "extract gtree-2.0-linux.tar\n"
	@tar xvf dep/archive/gtree-2.0-linux.tar --one-top-level=dep/gtree-2.0-linux > /dev/null

dep/archive/gtree-2.0-linux.tar :
	@$(call get_tar,https://github.com/jamjpan/GTreeJNI/releases/download/2.0/gtree-2.0-linux.tar)

####### Dependencies for Jargo Desktop #########################################

dep/com-sun-tools-visualvm-charts-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/api/com-sun-tools-visualvm-charts/RELEASE139/com-sun-tools-visualvm-charts-RELEASE139.jar)

dep/com-sun-tools-visualvm-uisupport-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/modules/com-sun-tools-visualvm-uisupport/RELEASE139/com-sun-tools-visualvm-uisupport-RELEASE139.jar)

dep/org-netbeans-lib-profiler-ui-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/modules/org-netbeans-lib-profiler-ui/RELEASE139/org-netbeans-lib-profiler-ui-RELEASE139.jar)

dep/org-netbeans-lib-profiler-charts-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/modules/org-netbeans-lib-profiler-charts/RELEASE139/org-netbeans-lib-profiler-charts-RELEASE139.jar)

dep/org-netbeans-modules-profiler-api-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/modules/org-netbeans-modules-profiler-api/RELEASE139/org-netbeans-modules-profiler-api-RELEASE139.jar)

dep/org-openide-util-lookup-RELEASE139.jar :
	@$(call get_jar,http://bits.netbeans.org/nexus/content/repositories/visualvm/com/sun/tools/visualvm/api/org-openide-util-lookup/RELEASE139/org-openide-util-lookup-RELEASE139.jar)

$(JFX) : | dep/javafx-sdk-13.0.1
	@printf "symlink $(@F)\n"
	@ln -s -t dep/ javafx-sdk-13.0.1/lib/$(@F)

dep/javafx-sdk-13.0.1 : | dep/archive/openjfx-13.0.1_linux-x64_bin-sdk.zip
	@printf "extract javafx-sdk-13.0.1\n"
	@unzip dep/archive/openjfx-13.0.1_linux-x64_bin-sdk.zip -d dep/ > /dev/null

dep/archive/openjfx-13.0.1_linux-x64_bin-sdk.zip :
	@$(call get_zip,https://download2.gluonhq.com/openjfx/13.0.1/openjfx-13.0.1_linux-x64_bin-sdk.zip)

