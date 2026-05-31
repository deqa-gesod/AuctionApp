// Interactive walkthrough of AuctionApp screen by screen.
// Each entry pairs a real screenshot with optional click hotspots that advance the flow.
(function () {
  'use strict';

  var SCREENSHOTS = 'screenshots/';

  var flows = {
    user: [
      { id: 'login', img: SCREENSHOTS + 'auctionapp-login.png', caption: 'Sign in', title: 'Sign in', desc: 'Login screen with Firebase Authentication. Email must be a Gmail address; password must be longer than eight characters.', hotspots: [{ x: 8, y: 56, w: 84, h: 11, label: 'Sign in', next: 1, primary: true }] },
      { id: 'list', img: SCREENSHOTS + 'auctionapp-product-list.png', caption: 'Browse active auctions', title: 'Product list', desc: 'Active auctions with image, deadline and current price. Data streams from Firebase Realtime Database, so when another user places a bid the price updates live.', hotspots: [{ x: 5, y: 12, w: 90, h: 9, label: 'Search', next: 2 }, { x: 5, y: 23, w: 90, h: 28, label: 'Open item', next: 3, primary: true }] },
      { id: 'search', img: SCREENSHOTS + 'auctionapp-search.png', caption: 'Search items', title: 'Search', desc: 'Filter the active auctions by name. Useful when the list grows past a handful of items.', hotspots: [{ x: 5, y: 23, w: 90, h: 28, label: 'Open item', next: 3, primary: true }] },
      { id: 'bid', img: SCREENSHOTS + 'auctionapp-place-bid.png', caption: 'Place a bid', title: 'Place bid', desc: 'Enter a max bid and tap PLACE BID. The bid is written to Firebase and broadcast to every connected client. When the deadline passes, ProductManager moves the item to sold_products and the highest bid wins.', hotspots: [{ x: 8, y: 80, w: 84, h: 10, label: 'Place bid', next: 4, primary: true }] },
      { id: 'profile', img: SCREENSHOTS + 'auctionapp-profile.png', caption: 'Your profile', title: 'Profile', desc: 'User profile with full name, phone, email and role. Edit or delete the account from here. Role-based access decides whether the admin dashboard is reachable.', hotspots: [{ x: 8, y: 79, w: 40, h: 10, label: 'Edit profile', next: 5, primary: true }] },
      { id: 'edit', img: SCREENSHOTS + 'auctionapp-edit-profile.png', caption: 'Edit profile', title: 'Edit profile', desc: 'Update name, phone or password. Writes go through the CRUD layer that talks to Firebase Realtime Database.', hotspots: [] }
    ],
    admin: [
      { id: 'login', img: SCREENSHOTS + 'auctionapp-login.png', caption: 'Sign in as admin', title: 'Sign in (admin)', desc: 'Admins use the same login screen. Role is read from the user record in Firebase Realtime Database after authentication.', hotspots: [{ x: 8, y: 56, w: 84, h: 11, label: 'Sign in', next: 1, primary: true }] },
      { id: 'dashboard', img: SCREENSHOTS + 'auctionapp-admin-dashboard.png', caption: 'Admin dashboard', title: 'Admin dashboard', desc: 'Two entry points: AUCTIONEER (manage products) and LOT (review bids and sold items).', hotspots: [{ x: 8, y: 48, w: 84, h: 11, label: 'Auctioneer', next: 2, primary: true }, { x: 8, y: 60, w: 84, h: 11, label: 'Lot', next: 4 }] },
      { id: 'addProduct', img: SCREENSHOTS + 'auctionapp-add-product-form.png', caption: 'Add product', title: 'Add product', desc: 'Admin uploads photos and fills in title, start price, location and deadline. Images go to Firebase Storage, metadata to Realtime Database.', hotspots: [{ x: 8, y: 86, w: 84, h: 10, label: 'Save', next: 3, primary: true }] },
      { id: 'records', img: SCREENSHOTS + 'auctionapp-post-records.png', caption: 'Post records', title: 'Post records', desc: 'Confirmation that the listing is live and visible to bidders.', hotspots: [{ x: 5, y: 5, w: 15, h: 8, label: 'Back', next: 1 }] },
      { id: 'offers', img: SCREENSHOTS + 'auctionapp-customer-offers.png', caption: 'Customer offers', title: 'Customer offers (Purchased tab)', desc: 'Expired auctions live in a separate sold_products node. Admin can download bid history as CSV, handy for reconciliation.', hotspots: [] }
    ]
  };

  var state = { flow: 'user', index: 0 };

  var els = {
    img: document.getElementById('screen-img'),
    caption: document.getElementById('screen-caption'),
    hotspots: document.getElementById('hotspots'),
    title: document.getElementById('screen-title'),
    desc: document.getElementById('screen-desc'),
    stepCurrent: document.getElementById('step-current'),
    stepTotal: document.getElementById('step-total'),
    back: document.getElementById('back-btn'),
    next: document.getElementById('next-btn'),
    restart: document.getElementById('restart-btn'),
    flowBtns: document.querySelectorAll('.flow-btn')
  };

  function render() {
    var screens = flows[state.flow];
    var screen = screens[state.index];

    var fade = els.img.hasAttribute('data-fade');
    var swap = function () {
      els.img.src = screen.img;
      els.img.alt = screen.title + ' screen of AuctionApp';
      if (fade) requestAnimationFrame(function () { els.img.style.opacity = '1'; });
    };
    if (fade) {
      els.img.style.opacity = '0';
      setTimeout(swap, 120);
    } else {
      swap();
    }

    if (els.caption) els.caption.textContent = screen.caption;
    els.title.textContent = screen.title;
    els.desc.textContent = screen.desc;
    els.stepCurrent.textContent = String(state.index + 1);
    els.stepTotal.textContent = String(screens.length);

    els.back.disabled = state.index === 0;
    els.next.disabled = state.index >= screens.length - 1;

    els.hotspots.innerHTML = '';
    (screen.hotspots || []).forEach(function (h) {
      var btn = document.createElement('button');
      btn.className = 'hotspot' + (h.primary ? ' is-primary' : '');
      btn.style.left = h.x + '%';
      btn.style.top = h.y + '%';
      btn.style.width = h.w + '%';
      btn.style.height = h.h + '%';
      btn.setAttribute('data-label', h.label);
      btn.setAttribute('aria-label', 'Tap: ' + h.label);
      btn.addEventListener('click', function () {
        if (typeof h.next === 'number') { state.index = h.next; render(); }
      });
      els.hotspots.appendChild(btn);
    });

    els.flowBtns.forEach(function (b) {
      var active = b.getAttribute('data-flow') === state.flow;
      b.classList.toggle('is-active', active);
      b.setAttribute('aria-selected', active ? 'true' : 'false');
    });
  }

  function next() { var s = flows[state.flow]; if (state.index < s.length - 1) { state.index += 1; render(); } }
  function back() { if (state.index > 0) { state.index -= 1; render(); } }
  function switchFlow(f) { if (!flows[f]) return; state.flow = f; state.index = 0; render(); }

  els.next.addEventListener('click', next);
  els.back.addEventListener('click', back);
  if (els.restart) els.restart.addEventListener('click', function () { state.index = 0; render(); });
  els.flowBtns.forEach(function (b) {
    b.addEventListener('click', function () { switchFlow(b.getAttribute('data-flow')); });
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'ArrowRight') next();
    else if (e.key === 'ArrowLeft') back();
  });

  function preload() {
    Object.keys(flows).forEach(function (k) {
      flows[k].forEach(function (s) { var i = new Image(); i.src = s.img; });
    });
  }

  render();
  preload();
})();
