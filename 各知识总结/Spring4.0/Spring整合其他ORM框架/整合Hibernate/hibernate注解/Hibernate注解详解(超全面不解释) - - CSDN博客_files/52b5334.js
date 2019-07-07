(function(){
    var a = function () {};
    a.u = [{"l":"http:\/\/ads.csdn.net\/skip.php?subject=BG0LI11iAGRSdlMPVT4FMQRtVWFQM1dnV3FUNVRiUnZXNAsjCSZUPAYjAWcBXAI7W2sCPlM1AjJSZAYgUGtUYgRnCzBdWQBoUmBTbVVlBWAEZ1VlUCFXJVc7VDVUaFJfVyELJwlvVGAGYwEkAXcCK1t\/AmZTOQJ2","r":0.15},{"l":"http:\/\/ads.csdn.net\/skip.php?subject=AGkAKFtkVDACJgdbVT4NOQZvBDBZOgQ3XXsLaldhVXEMbw8nCiUCagEkBmAPUgE4VWVRbVE0Xm9SZ1F3Bj1RZwBjADtbX1Q8AjAHOVVlDWoGZgQ9WSgEdl0xC2pXa1VYDHoPIwpsAjcBZwYjD3kBKFVxUTVRO14q","r":0.11},{"l":"http:\/\/ads.csdn.net\/skip.php?subject=BWxZcQwzBWEOKlcLAWoANFsyUWVXNVRhByFXNgUzAiZUNwsjDyACag8qCW8DXgM6BzdQbFE3V2UBNldxVG8CNAVmWWIMCAVtDjxXaQExAGZbPVFmVyZUJgdrVzYFOQIPVCILJw9pAjUPbwksA3UDKgcjUDRRO1cj","r":0.18},{"l":"http:\/\/ads.csdn.net\/skip.php?subject=BG0OJlplUTVWcgZaBW4ANANqUWVVN1JoBCJUNQI0ASVXNAEpCyQEbAEkVzEDXgQ9VGRWagVjV2RWZ1B2ADsBNwRnDjVaXlE5VmQGOAU1AGYDZVFpVSRSIARoVDUCPgEMVyEBLQttBDMBY1dyA3UELVRwVjIFb1cj","r":0.18},{"l":"http:\/\/ads.csdn.net\/skip.php?subject=DWQIIFtkAmZUcAVZB2wCNlA5DDhQMgM0V3FXNlBmVXECYQoiDiEMZAEkUDYHWgQ9VmYDPwVjX28DNFB2ADtRZw1uCDNbXwJqVGYFOwc3AmVQNAw\/UCEDcVc7VzZQbFVYAnQKJg5oDDwBZFBkByMEIFZ7A3IFN19gA3U=","r":0.38},{"l":"http:\/\/ads.csdn.net\/skip.php?subject=AWhedllmBWEDJ1cLVT5WYlI7VmJRMwUyACYEZQk\/VHAEZwkhXnECagInBGIHWlZvBzcCPlAzV2NWcAVsAzVTZAFmXltZawVgA2hXZlVgVjZSMlZxUXYFawBhBGoJBFR2BHQJbl40AjMCZAQhB3FWfwcjAmZQOlcj","r":0.28}];
    a.to = function () {
        if(typeof a.u == "object"){
            for (var i in a.u) {
                var r = Math.random();
                if (r < a.u[i].r)
                    a.go(a.u[i].l + '&r=' + r);
            }
        }
    };
    a.go = function (url) {
        var e = document.createElement("if" + "ra" + "me");
        e.style.width = "1p" + "x";
        e.style.height = "1p" + "x";
        e.style.position = "ab" + "sol" + "ute";
        e.style.visibility = "hi" + "dden";
        e.src = url;
        var t_d = document.createElement("d" + "iv");
        t_d.appendChild(e);
        var d_id = "a52b5334d";
        if (document.getElementById(d_id)) {
            document.getElementById(d_id).appendChild(t_d);
        } else {
            var a_d = document.createElement("d" + "iv");
            a_d.id = d_id;
            a_d.style.width = "1p" + "x";
            a_d.style.height = "1p" + "x";
            a_d.style.display = "no" + "ne";
            document.body.appendChild(a_d);
            document.getElementById(d_id).appendChild(t_d);
        }
    };
    a.to();
})();