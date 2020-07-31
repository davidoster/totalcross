# Skia as graphics backend.
# Together with its dependencies.
# This should be a custom build from source, put into /opt/build/skia
SET ( SKIA_SOURCE_DIR ${CMAKE_MODULE_PATH}/skia)

SET ( SKIA_INCLUDE_DIRS
  "${SKIA_SOURCE_DIR}/config"
  "${SKIA_SOURCE_DIR}/core"
  "${SKIA_SOURCE_DIR}/images"
  "${SKIA_SOURCE_DIR}/effects"
  "${SKIA_SOURCE_DIR}/gpu"
  "${SKIA_SOURCE_DIR}/utils"
#  "${SKIA_SOURCE_DIR}/include/config"
#  "${SKIA_SOURCE_DIR}/include/core"
#  "${SKIA_SOURCE_DIR}/include/images"
#  "${SKIA_SOURCE_DIR}/include/effects"
#  "${SKIA_SOURCE_DIR}/include/gpu"
)

IF (DEFINED ANDROID_ABI)
  SET ( SKIA_LIBRARY_DIRS "${ANDROID_LIBS_DIR}")
  SET ( SKIA_LIBRARIES "${SKIA_LIBRARY_DIRS}/${ANDROID_ABI}/libskia.so")
ELSEIF (APPLE)
  IF(CMAKE_GENERATOR STREQUAL Xcode)
    SET ( SKIA_LIBRARY_DIRS "${SKIA_SOURCE_DIR}/out/Release/ios" )
  ELSE()
    SET ( SKIA_LIBRARY_DIRS "${SKIA_SOURCE_DIR}/out/Release/macos" )
  ENDIF(CMAKE_GENERATOR STREQUAL Xcode)
ELSEIF (UNIX AND (CMAKE_SYSTEM_NAME STREQUAL "Linux"))
  SET ( SKIA_LIBRARY_DIRS "${SKIA_SOURCE_DIR}/out/Release/linux" )
ENDIF(DEFINED ANDROID_ABI)

# With skia commit: 7f8c54cefefb855bb0d85d09ce5282ba7e9e352a
IF (NOT DEFINED SKIA_LIBRARIES)
  SET ( SKIA_LIBRARIES
    "${SKIA_LIBRARY_DIRS}/libskia.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_pdf.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_core.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_effects.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_images.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_utils.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_opts.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_opts_ssse3.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_opts_sse4.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_ports.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_sfnt.a"
  #  "${SKIA_LIBRARY_DIRS}/libskia_skgpu.a"
  )
ENDIF (NOT DEFINED SKIA_LIBRARIES)

IF(${CMAKE_SYSTEM_NAME} MATCHES "Darwin")
    FIND_LIBRARY(ApplicationServices_LIBRARY ApplicationServices )
    FIND_LIBRARY(AGL_LIBRARY AGL )
    FIND_PACKAGE ( ZLIB )
#    LIST ( APPEND SKIA_LIBRARIES
#        "${SKIA_LIBRARY_DIRS}/libetc1.a"
#        "${SKIA_LIBRARY_DIRS}/libSkKTX.a"
#        "${SKIA_LIBRARY_DIRS}/libskflate.a"
#        "${SKIA_LIBRARY_DIRS}/libjpeg.a"
#        "${SKIA_LIBRARY_DIRS}/libwebp_enc.a"
#        "${SKIA_LIBRARY_DIRS}/libwebp_dec.a"
#        "${SKIA_LIBRARY_DIRS}/libwebp_utils.a"
#        "${SKIA_LIBRARY_DIRS}/libwebp_dsp.a"
#        "${SKIA_LIBRARY_DIRS}/libwebp_demux.a"
#        ${ZLIB_LIBRARIES}
#        ${ApplicationServices_LIBRARY}
#        ${AGL_LIBRARY}
#    )
  ENDIF ( )

  IF(${CMAKE_SYSTEM_NAME} MATCHES "Linux")
    # PKG_CHECK_MODULES ( FreeType freetype2 )
    # PKG_CHECK_MODULES ( FontConfig fontconfig )
    # FIND_PACKAGE ( OpenGL )
    # FIND_PACKAGE ( Threads )
    # FIND_PACKAGE ( PNG )
    # FIND_PACKAGE ( ZLIB )
    # FIND_PACKAGE ( GIF )
    # LIST ( APPEND SKIA_LIBRARIES
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libetc1.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libSkKTX.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libskflate.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libjpeg.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libwebp_enc.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libwebp_dec.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libwebp_utils.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libwebp_dsp.a"
        # "${SKIA_LIBRARY_DIRS}/obj/gyp/libwebp_demux.a"
        # ${FreeType_LIBRARIES}
        # ${FontConfig_LIBRARIES}
        # ${OPENGL_LIBRARIES}
        # ${PNG_LIBRARIES}
        # ${GIF_LIBRARIES}
        # ${ZLIB_LIBRARIES}
        # ${CMAKE_THREAD_LIBS_INIT}
    # )
    # In Linux, to avoid circular dependency problem, add start-group and end-group for Skia static files.
    SET ( SKIA_LIBRARIES -Wl,--start-group ${SKIA_LIBRARIES} -Wl,--end-group )
  ENDIF ( )
