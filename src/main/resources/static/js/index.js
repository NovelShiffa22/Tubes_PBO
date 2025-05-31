// Daftar semua mata kuliah
const allCourses = [
  "Agama",
  "Algoritma dan Pemrograman 1",
  "Kalkulus",
  "Logika Matematika",
  "Matematika Diskrit",
  "Pancasila",
  "Pendidikan Karakter",
  "Statistika",
  "Algoritma dan Pemrograman 2",
  "Bahasa Inggris",
  "Etika dalam AI",
  "Kalkulus Lanjut",
  "Matriks dan Ruang Vektor",
  "Organisasi dan Arsitektur Komputer",
  "Pemodelan Basis Data",
  "Analisis dan Perancangan PL",
  "Analisis Kompleksitas Algoritma",
  "Sistem Basis Data",
  "Sistem Operasi",
  "Struktur Data",
  "Teori Bahasa dan Automata",
  "Teori Peluang",
  "Dasar Kecerdasan Artifisial",
  "Interaksi Manusia dan Komputer",
  "Jaringan Komputer",
  "Pemrograman Berorientasi Objek",
  "Strategi Algoritma",
  "Wawasan Global TIK",
  "Sosio-Informatik dan Keprofesian",
  "Implementasi dan Pengujian PL",
  "Keamanan Siber",
  "Kecerdasan Artifisial",
  "Komputasi Awan dan Terdistribusi",
  "Manajemen Projek TIK",
  "Bahasa Indonesia",
  "Statika",
  "Pengenalan Pemrograman",
  "Aljabar Linear untuk Sains Data",
  "Algoritma dan Pemrograman",
  "Teori Peluang dan Implementasi",
  "Perancangan dan Implementasi Basis Data",
  "Analisi Kompleksitas Algoritma",
  "Sistem Manajemen Basis Data",
  "Pemodelan, Simulasi, dan Optimasi",
  "Infrastruktur dan Platform untuk Sains Data",
  "Kecerdasan Buatan",
  "Analisa Data",
  "Perancangan Aplikasi Sains Data",
  "Metode Visualisasi Data",
  "Pembelajaran Mesin",
  "Manajemen Proyek",
  "Keamanan Data",
  "Infrastruktur dan Teknologi Big Data",
  "Analisa Deret Waktu",
  "Rekayasa Sistem Informasi",
  "Sains Data untuk Masyarakat",
  "Pembentukan Karakter",
  "Algoritma Pemrograman",
  "Manajemen Proses Bisnis",
  "Pengantar Rekayasa Perangkat Lunak",
  "Dasar Pemrograman Berorientasi Objek",
  "Rekayasa Kebutuhan Perangkat Lunak",
  "Design Thinking",
  "Pemodelan Perangkat Lunak",
  "Proses Perangkat Lunak",
  "Keprofesian Rekayasa Perangkat Lunak",
  "Arsitektur dan Desain Perangkat Lunak",
  "Basis Data",
  "Dasar Jaringan Komputer",
  "Proyek Tingkat II",
  "Konstruksi Perangkat Lunak",
  "Kewirausahaan",
  "Pemrograman Perangkat Bergerak",
  "Pengalaman Pengguna (UX)",
  "Perancangan dan Pemrograman Web",
  "Penjaminan Mutu Perangkat Lunak",
  "Berpikir Komputasional & Pengenalan Pemrograman",
  "Pengantar Teknologi Informasi",
  "Statistika dan Analitik Data",
  "Aljabar Linear dan Matriks",
  "Pemeliharaan dan Adminstrasi Teknologi Informasi",
  "Sistem Multimedia",
  "Manajemen Layanan Teknologi Informasi",
  "Keterampilan dan Profesionalisme Teknologi Informasi",
  "Sistem Cerdas",
  "Teknologi Informasi Untuk Masyarakat",
  "Pengalaman Pengguna",
  "Pemrograman Web",
  "Pemrograman Platform & IOT",
  "Rekayasa Perangkat Lunak",
]

// Variabel global untuk menyimpan data
let allNotesData = []
let currentDisplayData = []

// Fungsi untuk inisialisasi aplikasi
function initializeApp() {
  console.log("Initializing app...")

  // Ambil elemen HTML berdasarkan ID
  const informatika = document.getElementById("informatika")
  const dataSains = document.getElementById("data-sains")
  const rekayasaPerangkatLunak = document.getElementById("rekayasa-perangkat-lunak")
  const teknologiInformasi = document.getElementById("teknologi-informasi")
  const courseSelect = document.getElementById("course")
  const cardGrid = document.getElementById("card-grid")
  const searchInput = document.getElementById("search-input")
  const searchButton = document.getElementById("search-button")
  const executeFilterButton = document.getElementById("execute-filter")

  // Pastikan elemen ditemukan
  if (!cardGrid) {
    console.error("Elemen dengan ID 'card-grid' tidak ditemukan di DOM.")
    return
  }
  if (!searchInput) {
    console.error("Elemen dengan ID 'search-input' tidak ditemukan di DOM.")
    return
  }
  if (!searchButton) {
    console.error("Elemen dengan ID 'search-button' tidak ditemukan di DOM.")
    return
  }

  // Tambahkan daftar mata kuliah ke dropdown
  if (courseSelect) {
    allCourses.forEach((course) => {
      const option = document.createElement("option")
      option.value = course
      option.textContent = course
      courseSelect.appendChild(option)
    })
  }

  // Setup event listeners
  setupEventListeners()

  // Load data awal
  loadNotes()
}

// Fungsi untuk setup event listeners
function setupEventListeners() {
  const searchInput = document.getElementById("search-input")
  const searchButton = document.getElementById("search-button")
  const clearSearchButton = document.getElementById("clear-search")
  const executeFilterButton = document.getElementById("execute-filter")

  // Event listener untuk search input (Enter key)
  if (searchInput) {
    searchInput.addEventListener("keypress", (event) => {
      if (event.key === "Enter") {
        handleSearch()
      }
    })
  }

  // Event listener untuk search button
  if (searchButton) {
    searchButton.addEventListener("click", handleSearch)
  }

  // Event listener untuk clear search button
  if (clearSearchButton) {
    clearSearchButton.addEventListener("click", resetSearch)
  }

  // Event listener untuk filter button
  if (executeFilterButton) {
    executeFilterButton.addEventListener("click", handleFilter)
  }
}

// Fungsi untuk handle search
function handleSearch() {
  const searchInput = document.getElementById("search-input")
  const searchTerm = searchInput.value.trim()

  if (!searchTerm) {
    // Jika search kosong, tampilkan semua data seperti saat pertama kali dimuat
    displayNotes(allNotesData)
    return
  }

  // Lakukan pencarian
  searchNotes(searchTerm)
}

// Fungsi untuk handle filter
function handleFilter() {
  const informatika = document.getElementById("informatika").checked
  const dataSains = document.getElementById("data-sains").checked
  const rekayasaPerangkatLunak = document.getElementById("rekayasa-perangkat-lunak").checked
  const teknologiInformasi = document.getElementById("teknologi-informasi").checked
  const course = document.getElementById("course").value
  const sortBy = document.getElementById("sort-by").value
  const sortOrder = document.getElementById("sort-order").value

  filterNotes(informatika, dataSains, rekayasaPerangkatLunak, teknologiInformasi, course, sortBy, sortOrder)
}

// Fungsi untuk memuat semua catatan
function loadNotes() {
  console.log("Memuat semua catatan...")
  const cardGrid = document.getElementById("card-grid")

  // Tampilkan loading indicator
  showLoadingIndicator()

  fetch("/api/data/getAllnotes", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      console.log("Response status:", response.status)
      if (!response.ok) {
        throw new Error(`Network response was not ok: ${response.status} ${response.statusText}`)
      }
      return response.json()
    })
    .then((data) => {
      console.log("Data diterima dari getAllnotes:", data)
      allNotesData = data
      currentDisplayData = data
      displayNotes(data)
    })
    .catch((error) => {
      console.error("Error fetching notes:", error)
      showErrorMessage(`Failed to load notes: ${error.message}`)
    })
}

// Fungsi untuk menampilkan loading indicator
function showLoadingIndicator() {
  const cardGrid = document.getElementById("card-grid")
  cardGrid.innerHTML = `
    <div class="col-span-full text-center py-8">
      <p class="text-gray-500">Loading notes...</p>
    </div>
  `
}

// Fungsi untuk menampilkan error message
function showErrorMessage(message) {
  const cardGrid = document.getElementById("card-grid")
  cardGrid.innerHTML = `
    <div class="col-span-full text-center py-8">
      <p class="text-red-500">${message}</p>
      <button onclick="loadNotes()" class="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
        Retry
      </button>
    </div>
  `
}

// Fungsi untuk menampilkan catatan
function displayNotes(data) {
  const cardGrid = document.getElementById("card-grid")

  if (!Array.isArray(data) || data.length === 0) {
    cardGrid.innerHTML = `
      <div class="col-span-full text-center py-8">
        <p class="text-gray-500">No notes available</p>
      </div>
    `
    return
  }

  cardGrid.innerHTML = "" // Kosongkan grid

  data.forEach((item) => {
    const noteCard = document.createElement("a")
    noteCard.href = `/note/view/${item.id}`
    noteCard.className =
      "flex h-[250px] flex-col text-left bg-white px-5 rounded-lg shadow-md transition-all duration-200 hover:translate-y-[-4px] hover:shadow-lg"

    noteCard.innerHTML = `
      <h3 class="text-2xl m-0 pt-2">${item.name || "Untitled"}</h3>
      <p class="font-semibold text-base text-[#555]">${item.course || "Unknown Course"} - ${item.major || "Unknown Major"}</p>
      <span class="font-extralight text-sm text-[#555] mb-2.5 pt-2">${item.username || "Anonymous"}</span>
      <div class="h-full mt-2.5 pb-5 flex flex-col justify-end items-baseline text-sm text-[#444]">
        <div class="flex justify-center items-center">
          <img class="h-[15px] w-[15px] mr-1" src="/img/star.png" alt="Rating">
          <p>${item.rating || 0}/5.0</p>
        </div>
        <div class="flex justify-center items-center">
          <img class="h-[15px] w-[15px] mr-1" src="/img/view.png" alt="Views">
          <p>${item.views || 0} Views</p>
        </div>
      </div>
    `

    cardGrid.appendChild(noteCard)
  })
}

// Fungsi untuk mencari catatan berdasarkan nama
async function searchNotes(searchTerm) {
  try {
    showLoadingIndicator()

    const response = await fetch(`/api/data/searchByNames?name=${encodeURIComponent(searchTerm)}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    })

    if (!response.ok) {
      throw new Error("Failed to fetch search results")
    }

    const data = await response.json()
    currentDisplayData = data
    displayNotes(data)
  } catch (error) {
    console.error("Error searching notes:", error)
    showErrorMessage("Failed to search notes")
  }
}

// Fungsi untuk filter catatan
async function filterNotes(IF, DS, RPL, IT, course, sortBy, order) {
  try {
    showLoadingIndicator()

    const params = new URLSearchParams({
      IF: IF,
      DS: DS,
      RPL: RPL,
      IT: IT,
      sortBy: sortBy || "letter",
      order: order || "asc",
    })

    if (course && course !== "All") {
      params.append("course", course)
    }

    const response = await fetch(`/api/data/filterNotes?${params}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    })

    if (!response.ok) {
      throw new Error("Failed to fetch filtered results")
    }

    const data = await response.json()
    currentDisplayData = data
    displayNotes(data)
  } catch (error) {
    console.error("Error filtering notes:", error)
    showErrorMessage("Failed to filter notes")
  }
}

// Fungsi untuk reset search dan tampilkan semua data
function resetSearch() {
  const searchInput = document.getElementById("search-input")
  searchInput.value = ""
  displayNotes(allNotesData)
}

// Fungsi untuk kompatibilitas dengan kode lama
function searchByName() {
  handleSearch()
}

function sortNotes() {
  handleFilter()
}

// Inisialisasi aplikasi ketika DOM sudah siap
document.addEventListener("DOMContentLoaded", initializeApp)

// Fallback jika DOMContentLoaded sudah terlewat
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeApp)
} else {
  initializeApp()
}
