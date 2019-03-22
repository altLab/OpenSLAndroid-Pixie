// Global Variables:
var videoOn = 0;
var debug_counter = 0;
var error_counter = 0;

var working = null;
var showing = null;

var timeout = 500;
var modoRobot = 0;
var timerJson = null;
var langpref = "en";
var minimun = 10;

function preloadImages(files, cb) {
  var len = files.length;
  $(
    $.map(files, function(f) {
      return '<img src="' + f + '" />';
    }).join("")
  ).load(function() {
    if (--len === 0) {
      cb();
    }
  });
}

function setSensorImage(base, value, prefix) {
  var step = 5,
    sLevel = 1;

  if (value < minimun) {
    sLevel = 0;
  } else if (value < minimun + step) {
    sLevel = 1;
  } else if (value < minimun + step * 2) {
    sLevel = 2;
  } else if (value < minimun + step * 3) {
    sLevel = 3;
  } else if (value < minimun + step * 4) {
    sLevel = 4;
  } else {
    sLevel = 5;
  }
  $("#" + base).attr("src", prefix + sLevel + ".png");
}

function updateVolSensors(l, r) {
  setSensorImage("leftvol", l, "sl");
  setSensorImage("rightvol", r, "sr");
}

function setBatteryIcon(base, level) {
  var bLevel = 1;

  if (level < 10) {
    bLevel = 5;
  } else if (level < 25) {
    bLevel = 4;
  } else if (level < 50) {
    bLevel = 3;
  } else if (level < 75) {
    bLevel = 2;
  } else {
    bLevel = 1;
  }
  $("#" + base).attr("src", "bat" + bLevel + ".png");
}

function setTouchSensorImage(value) {
  var state = 0;
  if (value == 1) {
    state = 1;
  } else {
    state = 0;
  }
  $("#touchsensor").attr("src", "top" + state + ".png");
}

function initVideo() {
  working = $("#img1");
  showing = $("#img2");
}

function loadImage(e) {
  var oldshowing = showing;
  showing = working;
  working = oldshowing;
  showing.unbind();
  showing.css("zIndex", 1);
}

function processVideo() {
  if (videoOn === 1) {
    working.css("zIndex", -1);
    working.load(loadImage);
    working.attr("src", "live.jpg?rnd=" + Math.floor(Math.random() * 1000000));
  }
}

function isEmpty(obj) {
  if (typeof obj == "undefined" || obj === null || obj === "") {
    return true;
  }
  if (typeof obj == "number" && isNaN(obj)) {
    return true;
  }
  if (obj instanceof Date && isNaN(Number(obj))) {
    return true;
  }
  return false;
}

function updateInformationJson() {
  $.getJSON("info.json", function(jd, status, jqXHR) {
    $("#android_model").text(jd.android.model);
    $("#android_datetime").text(
      jd.android.datetime.replace(
        /^(\d{4})(\d\d)(\d\d) (\d\d)(\d\d)(\d\d)$/,
        "$1-$2-$3 $4:$5:$6"
      )
    );
    $("#android_battery").text(jd.android.battery + " %");
    setBatteryIcon("android_battery_icon", jd.android.battery);
    if (!isEmpty(jd.android.wifi)) {
      $("#nettype_label").text("Wifi: ");
      $("#android_level").text(
        jd.android.wifi + " % (" + jd.android.wifispeed + " Mbps)"
      );
    } else {
      $("#nettype_label").text(window.lang.convert("Mobile: "));
      $("#android_level").text(
        jd.android.mobile + " % (" + jd.android.mobiletype + ")"
      );
    }

    // $("#score").text(jd.android.score);
    $("#debug_counter").text(debug_counter++);

    // if (undefined != jd.browser.timeout) {
    // if (timeout != jd.browser.timeout) {
    // timeout = jd.browser.timeout;
    // alert("new Timeout");
    // timerJson.reset();
    // timerJson.set({
    // time : timeout,
    // autostart : true
    // });
    // }
    // }

    // $("#dump").text(jqXHR.responseText);

    if (!isEmpty(jd.android.sliceimage)) {
      $("#printImage").attr("src", jd.android.sliceimage);
    }

    if (!isEmpty(jd.android.slicestotal)) {
      $("#slices_total").text(jd.android.slicestotal);
    }
    if (!isEmpty(jd.android.slicescurrent)) {
      $("#slices_current").text(jd.android.slicescurrent);
    }
  })
    .success(function() {})
    .error(function() {
      // if (timerJson.isActive()) {
      $("#error_counter").text(error_counter++);
      $("#dump").text(status);
      overlay = $("<div></div>")
        .prependTo("body")
        .attr("id", "overlay");
      alert(
        window.lang.convert("Error communicating to webserver - disable timer")
      );
      timerJson.toggle();
      // } else {
      // alert("Still calling!");
      // }
    })
    .complete(function() {});
}

// Initialization routines:

function initResources() {
  preloadImages([], function() {});

  $(document).ready(function() {
    $("body").queryLoader2({
      barColor: "#0000ff",
      backgroundColor: "#ffffff",
      percentage: true,
      barHeight: 5,
      completeAnimation: "fade", // grow
      minimumTime: 100
    });
  });
}

function initLang() {
  window.lang = new jquery_lang_js();

  $().ready(function() {
    window.lang.run();
    window.lang.change(langpref);
  });
}

function initButtons() {
  $("#move01").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: 1
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
    // alert('Left');
  });

  $("#move10").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: 100
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#move100").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: 1000
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#move-01").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: -1
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#move-10").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: -100
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#move-100").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "move",
        unit: -1000
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#setZero").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "setzero"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#print").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "print"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#home").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "home"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#pause").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "pause"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#cancel").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "cancel"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#show").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "showimage",
        n: 1
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });
  $("#hide").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "showimage",
        n: 0
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#previous").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "showimage",
        n: "prev"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });

  $("#next").on("click", function() {
    $.get(
      "command.cgi",
      {
        action: "showimage",
        n: "next"
      },
      function(data) {
        // alert("Data Loaded: " + data);
      }
    );
  });
}

// function initKeys() {
//   $(document).keydown(function(e) {
//     if (modoRobot != 0) {
//       switch (e.which) {
//         case 37: // LEFT
//           $("#goleft").addClass("active");
//           $("#goleft").trigger("click");
//           break;

//         case 38: // UP
//           $("#goforward").addClass("active");
//           $("#goforward").trigger("click");
//           break;

//         case 39: // RIGHT
//           $("#goright").addClass("active");
//           $("#goright").trigger("click");
//           break;

//         case 40: // DOWN
//           $("#stop").addClass("active");
//           $("#stop").trigger("click");
//           break;

//         default:
//           return; // allow other keys to be handled
//       }
//       e.preventDefault();
//     } else {
//       return;
//     }
//   });

//   $(document).keyup(function(e) {
//     if (modoRobot != 0) {
//       switch (e.which) {
//         case 37: // LEFT
//           $("#goleft").removeClass("active");
//           break;

//         case 38: // UP
//           $("#goforward").removeClass("active");
//           break;

//         case 39: // RIGHT
//           $("#goright").removeClass("active");
//           break;

//         case 40: // DOWN
//           $("#stop").removeClass("active");
//           break;

//         default:
//           return; // allow other keys to be handled
//       }
//       e.preventDefault();
//     } else {
//       return;
//     }
//   });
// }

function initVideoParameters() {
  // Obtain video settings
  $.ajaxSetup({
    async: false
  });
  $.getJSON("info.json", function(jd, status, jqXHR) {
    if (undefined != jd.android.video) {
      videoOn = jd.android.video;
    }
    if (undefined != jd.browser.timeout) {
      timeout = jd.browser.timeout;
    }
  })
    .success(function() {
      // if (videoOn == 1) {
      // $(initVideo);
      // }
    })
    .error(function() {
      $("#error_counter").text(error_counter + 1);
      $("#dump").text(status);
    })
    .complete(function() {});
  $.ajaxSetup({
    async: true
  });
}

function initTimers() {
  // Setup Timers
  timerJson = $.timer(function() {
    $(updateInformationJson);
  });
  timerJson.set({
    time: timeout,
    autostart: true
  });
}

function initialize() {
  $(initResources);
  $(initLang);
  $(initButtons);
  //$(initKeys);
  $(initVideoParameters);
  $(initTimers);
}
